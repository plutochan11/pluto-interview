package com.pluto.pluto_interview.service;

import com.pluto.pluto_interview.enums.ErrorMessage;
import com.pluto.pluto_interview.event.TokenCreatedEvent;
import com.pluto.pluto_interview.exception.*;
import com.pluto.pluto_interview.model.Response;
import com.pluto.pluto_interview.model.Settings;
import com.pluto.pluto_interview.model.User;
import com.pluto.pluto_interview.model.dto.AuthenticationRequest;
import com.pluto.pluto_interview.model.dto.AuthenticationResult;
import com.pluto.pluto_interview.model.vo.RefreshTokenResult;
import com.pluto.pluto_interview.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.validation.ConstraintViolationException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class AuthenticationService {
	private final UserRepository userRepo;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final ApplicationEventPublisher appEventPublisher;
	private final CacheService cacheService;

	public static final String TOKEN_CACHE_KEY_PREFIX = "token:userId:";
	public static final String REFRESH_TOKEN_CACHE_KEY_PREFIX = "refreshToken:userId:";
	public static final String USER_ID_CLAIM_KEY = "userId";

	public AuthenticationService(UserRepository userRepo, PasswordEncoder passwordEncoder, JwtService jwtService, ApplicationEventPublisher appEventPublisher, CacheService cacheService) {
		this.userRepo = userRepo;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.appEventPublisher = appEventPublisher;
		this.cacheService = cacheService;
	}

	/**
	 * Register user
	 * @param authRequest A request containing relevant authentication information.
	 * @return A {@link Response} containing authentication tokens.
	 * @throws EmailAlreadyRegisteredException if the email is already registered.
	 */
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
		User user = User.newUser(email, password, username);

		User savedUser = null;
		try {
			savedUser = userRepo.save(user);
			// Throw an exception if the unique constraint is violated (rarely happens under high concurrency)
		} catch (DataIntegrityViolationException | ConstraintViolationException e){
			throw new EmailRegisteredException(ErrorMessage.EMAIL_REGISTERED.getErrorMessage());
		}

		// Create JWT token and refresh token
		Map<String, Object> claims = Map.of(USER_ID_CLAIM_KEY, user.getId());
		String token = jwtService.generate(claims, null, null);
		String refreshToken = jwtService.generateRefreshToken(claims);

		// Publish events with the created tokens
		String tokenCacheKey = TOKEN_CACHE_KEY_PREFIX + user.getId();
		String refreshTokenCacheKey = REFRESH_TOKEN_CACHE_KEY_PREFIX + user.getId();
		appEventPublisher.publishEvent(new TokenCreatedEvent(this, token, tokenCacheKey));
		appEventPublisher.publishEvent(new TokenCreatedEvent(this, refreshToken, refreshTokenCacheKey));

		// Publish events with the created user
		appEventPublisher.publishEvent(new UserCreatedEvent(this, savedUser));

		// Create VO
		AuthenticationResult authResult = new AuthenticationResult(user.getUsername(), token, refreshToken);

		log.info("New user registered (ID: {})", savedUser.getId());
		return Response.ok(authResult);
	}

	@Async
	@Transactional
	public CompletableFuture<Response> login(AuthenticationRequest credential) {
		// Verify user credentials
		User registeredUser = userRepo.findByEmail(credential.email())
			  .orElseThrow(() ->
			      new EmailNotRegisteredException(ErrorMessage.EMAIL_NOT_REGISTERED.getErrorMessage()));
		if (!passwordEncoder.matches(credential.password(), registeredUser.getPassword())) {
			throw new WrongPasswordException(ErrorMessage.WRONG_PASSWORD.getErrorMessage(), credential.email());
		}

		// Create JWT token and refresh token
		Map<String, Object> claims = Map.of(USER_ID_CLAIM_KEY, registeredUser.getId());
		String token = jwtService.generate(claims, null, null);
		String refreshToken = jwtService.generateRefreshToken(claims);

		// Publish event
		String tokenCacheKey = TOKEN_CACHE_KEY_PREFIX + registeredUser.getId();
		String refreshTokenCacheKey = REFRESH_TOKEN_CACHE_KEY_PREFIX + registeredUser.getId();
		appEventPublisher.publishEvent(new TokenCreatedEvent(this, token, tokenCacheKey));
		appEventPublisher.publishEvent(new TokenCreatedEvent(this, refreshToken, refreshTokenCacheKey));

		// Create VO, log and complete future
		AuthenticationResult authenticationResult = new AuthenticationResult(registeredUser.getUsername(), token, refreshToken);

		log.info("User(ID: {}) logged in", registeredUser.getId());
		return CompletableFuture.completedFuture(Response.ok(authenticationResult));
	}

	public Response logout(String token) {
		// Invalidate tokens
		Claims claims = jwtService.parse(token);
		Long userId = claims.get(USER_ID_CLAIM_KEY, Long.class);
		String tokenKey = TOKEN_CACHE_KEY_PREFIX + userId;
		String refreshTokenKey = REFRESH_TOKEN_CACHE_KEY_PREFIX + userId;
		cacheService.delete(tokenKey);
		cacheService.delete(refreshTokenKey);

		log.info("User(ID: {}) logged out", userId);
		return Response.ok();
	}

	/**
	 * Generate a new JWT token based on the {@code refreshToken}
	 * @param refreshToken The refresh token used to generate a new JWT token
	 * @return A {@link Response} containing a {@link RefreshTokenResult} with the new JWT token.
	 */
	public Response refreshToken(String refreshToken) {
		// Validate the refresh token
		Claims refreshTokenClaims = jwtService.parse(refreshToken);
		Long userId = refreshTokenClaims.get(USER_ID_CLAIM_KEY, Long.class);
		String key = REFRESH_TOKEN_CACHE_KEY_PREFIX + userId;
		String storedRefreshToken = cacheService.get(key);

		if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
			throw new UserNotLoggedInException(ErrorMessage.NOT_LOGGED_IN.getErrorMessage());
		}

		// Create a new JWT token
		Map<String, Object> claims = Map.of(USER_ID_CLAIM_KEY, userId);
		String newToken = jwtService.generate(claims, null, null);

		// Create new refresh token if about to expire (e.g. less than 1 day)
		String newRefreshToken = null;
		Date expiry = jwtService.getExpiry(refreshToken);
		if (Duration.between(Instant.now(), expiry.toInstant()).toDays() < 1) {
			newRefreshToken = jwtService.generateRefreshToken(claims);
		}

		RefreshTokenResult result = new RefreshTokenResult(newToken, newRefreshToken);

		// Log
		log.info("User(ID: {}) refreshed JWT token", userId);

		return Response.ok(result);
	}

	@Getter
	public static class UserCreatedEvent extends ApplicationEvent {
		private final User user;

		public UserCreatedEvent(Object source, User user) {
			super(source);
			this.user = user;
		}
	}
}
