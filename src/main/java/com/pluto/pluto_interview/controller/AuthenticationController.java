package com.pluto.pluto_interview.controller;

import com.pluto.pluto_interview.model.Response;
import com.pluto.pluto_interview.model.dto.AuthenticationRequest;
import com.pluto.pluto_interview.service.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.concurrent.*;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {
	private final AuthenticationService authService;
	private final Executor executor;

	private final Duration SERVICE_TIMEOUT;
	private final TimeUnit TIMEOUT_UNIT = TimeUnit.SECONDS;

	public AuthenticationController(
		AuthenticationService authService, 
		Executor executor,
	    @Value("${service.auth.timeout}") Duration serviceTimeout) 
	{
		this.authService = authService;
		this.executor = executor;

		// Initialise SERVICE_TIMEOUT with a default value if not provided
		if (serviceTimeout == null) {
			serviceTimeout = Duration.ofSeconds(10);
		}
		SERVICE_TIMEOUT = serviceTimeout;
	}

	@PostMapping("/register")
	public CompletableFuture<ResponseEntity<Response>> register(
		  @Valid @RequestBody AuthenticationRequest authRequest){

		return CompletableFuture.supplyAsync(() -> 
			authService.register(authRequest), executor)
			  .thenApply(response -> ResponseEntity.status(HttpStatus.CREATED).body(response))
			  .orTimeout(SERVICE_TIMEOUT.toSeconds(), TIMEOUT_UNIT);
	}

	@PostMapping("/login")
	public CompletableFuture<ResponseEntity<Response>> login(
		  @Valid @RequestBody AuthenticationRequest authRequest){

		return CompletableFuture.supplyAsync(() -> 
			authService.login(authRequest), executor)
			  .thenApply(ResponseEntity::ok)
			  .orTimeout(SERVICE_TIMEOUT.toSeconds(), TIMEOUT_UNIT);
	}

	// TODO Refine. Now user ID can be retrieved from security context
	@GetMapping("/refresh-token")
	public CompletableFuture<ResponseEntity<Response>> refreshToken(
		@RequestParam String refreshToken)
	{
		return CompletableFuture.supplyAsync(() -> 
			authService.refreshToken(refreshToken), executor)
			.thenApply(ResponseEntity::ok)
			.orTimeout(SERVICE_TIMEOUT.toSeconds(), TIMEOUT_UNIT);
	}

	@PatchMapping("/logout")
	public ResponseEntity<Response> logout() {
		Long userId = 
			(Long) SecurityContextHolder.getContext()
				.getAuthentication()
				.getPrincipal();
		CompletableFuture.runAsync(() -> authService.logout(userId), executor)
			  .orTimeout(SERVICE_TIMEOUT.toSeconds(), TIMEOUT_UNIT);
		return ResponseEntity.ok(Response.ok());
	}
}
