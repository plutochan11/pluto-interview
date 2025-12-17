package com.pluto.pluto_interview.service;

import com.pluto.pluto_interview.annotation.Log;
import com.pluto.pluto_interview.config.properties.JwtProperties;
import com.pluto.pluto_interview.enums.DifficultyLevel;
import com.pluto.pluto_interview.enums.ErrorMessage;
import com.pluto.pluto_interview.event.TokenGeneratedEvent;
import com.pluto.pluto_interview.exception.EmailAlreadyUsedException;
import com.pluto.pluto_interview.exception.EmailNotRegisteredException;
import com.pluto.pluto_interview.exception.WrongPasswordException;
import com.pluto.pluto_interview.model.Response;
import com.pluto.pluto_interview.model.Settings;
import com.pluto.pluto_interview.model.User;
import com.pluto.pluto_interview.model.dto.AuthenticationDto;
import com.pluto.pluto_interview.model.dto.AuthenticationResult;
import com.pluto.pluto_interview.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final JwtProperties jwtProperties;
	private final TokenService tokenService;
	private final ApplicationEventPublisher eventPublisher;

	@Transactional
	public Response register(@Validated AuthenticationDto authDto) {
		// Check if the email is already used
		String email = authDto.email();
		userRepository.findByEmail(email)
			  .ifPresent(user -> {
				  throw new EmailAlreadyUsedException(ErrorMessage.EMAIL_ALREADY_USED.getErrorMessage());
			  });

		// Save a new user record with a user settings
		String defaultUsername = email.substring(0, email.indexOf("@"));
		String encodedPassword = passwordEncoder.encode(authDto.password());

		User user = User.builder()
			  .username(defaultUsername)
			  .password(encodedPassword)
			  .email(email)
			  .build();
		Settings settings = Settings.builder()
			  .preferredDifficultyLevel(DifficultyLevel.MEDIUM.getDifficultyLevel())
			  .user(user)
			  .build();
		user.setSettings(settings);
		User savedUser = userRepository.save(user);

		// Assemble a Response and save the token to the cache
		AuthenticationResult authResult = generateAuthenticationResult(savedUser);

		log.info("New user registered (ID: {})", savedUser.getId());
		return Response.ok(authResult);
	}

	@Transactional
	public Response login(@Validated AuthenticationDto authDto) {
		// Verify user credentials
		User registeredUser = userRepository.findByEmail(authDto.email())
			  .orElseThrow(() ->
			      new EmailNotRegisteredException(ErrorMessage.EMAIL_NOT_REGISTERED.getErrorMessage()));
		if (!passwordEncoder.matches(authDto.password(), registeredUser.getPassword())) {
			throw new WrongPasswordException(ErrorMessage.WRONG_PASSWORD.getErrorMessage(), authDto.email());
		}

		// Generate a JWT token
		// Assemble a Response
		AuthenticationResult authenticationResult = generateAuthenticationResult(registeredUser);

		log.info("User(ID: {}) logged in", registeredUser.getId());
		return Response.ok(authenticationResult);
	}

	@Log
	public void logout(String token) {
		String userId = tokenService.invalidate(token);
		log.info("User(ID: {}) logged out", userId);
	}

	private AuthenticationResult generateAuthenticationResult(User user) {
		Map<String, Object> claims = Map.of(
			  "userId", user.getId()
		);
		String token = jwtService.generateToken(claims);
//		tokenService.save(token);
		// Public a TokenGeneratedEvent and have relevant listeners handle any follow-up actions
		eventPublisher.publishEvent(new TokenGeneratedEvent(this, token));

		long expiresIn = jwtProperties.getTtl();
		return new AuthenticationResult(user.getUsername(), token, expiresIn);
	}
}
