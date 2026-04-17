package com.pluto.pluto_interview.service;

import com.pluto.pluto_interview.config.properties.JwtProperties;
import com.pluto.pluto_interview.constant.TokenProperty;
import com.pluto.pluto_interview.event.UserCreatedEvent;
import com.pluto.pluto_interview.event.UserLoggedInEvent;
import com.pluto.pluto_interview.event.UserLoggedOutEvent;
import com.pluto.pluto_interview.enums.ErrorMessage;
import com.pluto.pluto_interview.event.TokensCreatedEvent;
import com.pluto.pluto_interview.exception.*;
import com.pluto.pluto_interview.model.Response;
import com.pluto.pluto_interview.model.User;
import com.pluto.pluto_interview.model.dto.AuthenticationRequest;
import com.pluto.pluto_interview.model.dto.AuthenticationResult;
import com.pluto.pluto_interview.model.vo.RefreshTokenResult;
import com.pluto.pluto_interview.repository.UserRepository;
import com.pluto.pluto_interview.util.IdGenerator;
import io.jsonwebtoken.Claims;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class AuthenticationService {
	private final UserRepository userRepo;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final JwtProperties jwtProperties;
	private final ApplicationEventPublisher appEventPublisher;
	private final CacheService cacheService;
	private final RedissonClient redissonClient;
	private final IdGenerator idGenerator;

	private static final String LOCK_NAME_PREFIX = "lock:auth-service:user-id:";
	public static final String USER_ID_CLAIM_KEY = "userId";

	public AuthenticationService(UserRepository userRepo, PasswordEncoder passwordEncoder, JwtService jwtService,
	                             JwtProperties jwtProperties, ApplicationEventPublisher appEventPublisher,
	                             CacheService cacheService, RedissonClient redissonClient, IdGenerator idGenerator) {
		this.userRepo = userRepo;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.jwtProperties = jwtProperties;
		this.appEventPublisher = appEventPublisher;
		this.cacheService = cacheService;
		this.redissonClient = redissonClient;
		this.idGenerator = idGenerator;
	}

	/**
	 * Register user
	 * @param authRequest A request containing relevant authentication information.
	 * @return A {@link Response} containing authentication tokens.
	 * @throws EmailAlreadyRegisteredException if the email is already registered.
	 */
	@Transactional
	public Response register(AuthenticationRequest authRequest) {
		// Check the presence of the email
		String email = authRequest.email();
		userRepo.findByEmail(email)
			  // If present, throw an exception
			  .ifPresent(user -> {
				  throw new EmailAlreadyRegisteredException(ErrorMessage.EMAIL_ALREADY_REGISTERED.getErrorMessage());
			  });

		// Add the new user to the database
		// The username defaults to the domain part of the register email.
		String username = email.substring(0, email.indexOf("@"));
		String password = passwordEncoder.encode(authRequest.password());
		User user = User.newUser(idGenerator.nextId(), email, password, username);

		User savedUser;
		try {
			savedUser = userRepo.save(user);
			// Throw an exception if the unique constraint is violated (rarely happens under high concurrency)
		} catch (DataIntegrityViolationException | ConstraintViolationException e){
			throw new EmailRegisteredException(ErrorMessage.EMAIL_REGISTERED.getErrorMessage());
		}
		Long savedUserId = Objects.requireNonNull(savedUser.getId(), "Saved user ID cannot be null.");

		// Create JWT token and refresh token
		Map<String, Object> claims = Map.of(USER_ID_CLAIM_KEY, savedUserId);
		String token = jwtService.generate(claims, null, null);
		String refreshToken = jwtService.generateRefreshToken(claims);

		// Publish events with the created tokens
		appEventPublisher.publishEvent(new TokensCreatedEvent(this, token, getAccessTokenTtlSeconds(),
			  refreshToken, getRefreshTokenTtlSeconds(), savedUserId));

		// Publish events with the created user
		appEventPublisher.publishEvent(new UserCreatedEvent(this, savedUser));

		// Create VO
		AuthenticationResult authResult = new AuthenticationResult(user.getUsername(), token, refreshToken);

		log.info("New user registered (ID: {})", savedUserId);
		return Response.ok(authResult);
	}

	public Response login(AuthenticationRequest authRequest) {
		// Verify user's presence
		User user = getUser(authRequest);

		//  Check user's login status to avoid multiple logins
		if (isLoggedIn(user.getId())) {
			throw new UserAlreadyLoggedInException(ErrorMessage.ALREADY_LOGGED_IN.getErrorMessage());
		}

		// Verify password correctness
		if (!passwordEncoder.matches(authRequest.password(), user.getPassword())) {
			// If incorrect, throw an exception
			throw new WrongPasswordException(ErrorMessage.WRONG_PASSWORD.getErrorMessage(), authRequest.email());
		}

		// Acquire lock to avoid multiple logins
//		Lock lock = new SimpleRedisLock(stringRedisTemplate);
		RLock lock = redissonClient.getLock(LOCK_NAME_PREFIX + user.getId());
		try {
			if (!lock.tryLock(3, TimeUnit.SECONDS)) {
				throw new LockTimeoutException(ErrorMessage.TIMEOUT.getErrorMessage());
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new UnknownTokenException(e);
		}

		String token;
		String refreshToken;
		try {
			// Create JWT token and refresh token
			Map<String, String> tokens = generateTokenAndRefreshToken(user.getId());
			token = tokens.get("token");
			refreshToken = tokens.get("refreshToken");
			// Publish events with the created tokens
			appEventPublisher.publishEvent(new TokensCreatedEvent(this, token, getAccessTokenTtlSeconds(),
				  refreshToken, getRefreshTokenTtlSeconds(), user.getId()));

			// Publish events with the logged in user
			appEventPublisher.publishEvent(new UserLoggedInEvent(this, user));
		} finally {
			lock.unlock();
		}

		// Create VO
		AuthenticationResult authResult = new AuthenticationResult(user.getUsername(), token, refreshToken);

		log.info("User(ID: {}) logged in", user.getId());
		return Response.ok(authResult);
	}

	public void logout(Long userId) {

		// Publish a UserLoggedOutEvent to have relevant parties handle logout synchronously.
		appEventPublisher.publishEvent(new UserLoggedOutEvent(this, userId));
		log.info("User(ID: {}) logged out", userId);
	}

	/**
	 * Generate a new JWT token based on the {@code refreshToken}
	 * @return A {@link Response} containing a {@link RefreshTokenResult} with the new JWT token.
	 */
	public Response refreshToken(String refreshToken) {
		// Validate the refresh token
		Claims refreshTokenClaims = jwtService.parse(refreshToken);
		Long userId = refreshTokenClaims.get(USER_ID_CLAIM_KEY, Long.class);
		String key = TokenProperty.REFRESH_TOKEN_KEY_PREFIX + userId;
		String storedRefreshToken = cacheService.getString(key);

		if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
			throw new UserNotLoggedInException(ErrorMessage.NOT_LOGGED_IN.getErrorMessage());
		}

//		// Get refresh token from the cache
//		String refreshToken = stringRedisTemplate.opsForValue().get(key);

		// Create a new JWT token
		Map<String, Object> claims = Map.of(USER_ID_CLAIM_KEY, userId);
		String newToken = jwtService.generate(claims, null, null);

		// Create new refresh token if about to expire (e.g. less than 1 day)
		String newRefreshToken = null;
		Date expiry = jwtService.getExpiry(refreshToken);
		Duration remainingRefreshTokenTtl = Duration.between(Instant.now(), expiry.toInstant());
		if (remainingRefreshTokenTtl.toDays() < 1) {
			newRefreshToken = jwtService.generateRefreshToken(claims);
			remainingRefreshTokenTtl = jwtProperties.getRefreshTokenTtl();
		}

		String refreshTokenToCache = newRefreshToken != null ? newRefreshToken : refreshToken;
		appEventPublisher.publishEvent(new TokensCreatedEvent(
			  this,
			  newToken,
			  getAccessTokenTtlSeconds(),
			  refreshTokenToCache,
			  Math.max(1, remainingRefreshTokenTtl.toSeconds()),
			  userId
		));

		RefreshTokenResult result = new RefreshTokenResult(newToken, newRefreshToken);

		// Log
		log.info("User(ID: {}) refreshed JWT token", userId);

		return Response.ok(result);
	}

	private boolean isLoggedIn(Long userId) {
		String key = TokenProperty.TOKEN_KEY_PREFIX + userId;
		return cacheService.containsKey(key);
	}

	private User getUser(AuthenticationRequest authRequest) {
		return userRepo.findByEmail(authRequest.email())
			  // If absent, throw an exception
			  .orElseThrow(() ->
				    new EmailNotRegisteredException(ErrorMessage.EMAIL_NOT_REGISTERED.getErrorMessage()));
	}

	/**
	 * Generate JWT token and refresh token
	 * @return A {@link Map} containing the generated tokens. The token is under the key "token" and
	 * the refresh token is under the key "refresh
	 */
	private Map<String, String> generateTokenAndRefreshToken(Long userId) {
		Map<String, Object> claims = Map.of(USER_ID_CLAIM_KEY, userId);
		String token = jwtService.generate(claims, null, null);
		String refreshToken = jwtService.generateRefreshToken(claims);
		return Map.of("token", token, "refreshToken", refreshToken);
	}

	private long getAccessTokenTtlSeconds() {
		return Duration.of(jwtProperties.getTtl(), jwtProperties.getTimeUnit().toChronoUnit()).toSeconds();
	}

	private long getRefreshTokenTtlSeconds() {
		return jwtProperties.getRefreshTokenTtl().toSeconds();
	}
}
