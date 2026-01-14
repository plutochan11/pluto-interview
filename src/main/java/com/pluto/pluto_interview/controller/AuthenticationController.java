package com.pluto.pluto_interview.controller;

import com.pluto.pluto_interview.model.Response;
import com.pluto.pluto_interview.model.dto.AuthenticationCredential;
import com.pluto.pluto_interview.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthenticationController {
	private final AuthenticationService service;
	private final Executor executor;
	private final int SERVICE_TIMEOUT_SECONDS = 20;

	@PostMapping("/register")
	public CompletableFuture<ResponseEntity<Response>> register(
		  @Valid @RequestBody AuthenticationCredential credential) throws ExecutionException,
		  InterruptedException, TimeoutException {
		return service.register(credential)
			  .orTimeout(SERVICE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
			  .thenApply(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
	}

	@PostMapping("login")
	public CompletableFuture<ResponseEntity<Response>> login(
		  @Valid @RequestBody AuthenticationCredential credential) throws ExecutionException,
		  InterruptedException, TimeoutException {
		return service.login(credential)
			  .orTimeout(SERVICE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
			  .thenApply(ResponseEntity::ok);
	}

	@GetMapping("/refresh-token")
	public CompletableFuture<ResponseEntity<Response>> refreshToken(@RequestParam String refreshToken)
		  throws ExecutionException, InterruptedException, TimeoutException {
		return CompletableFuture.supplyAsync(() -> service.refreshToken(refreshToken), executor)
			  .orTimeout(SERVICE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
			  .thenApply(ResponseEntity::ok);
	}

	/**
	 * Log out user
	 * @return
	 */
	@PatchMapping("/logout")
	public CompletableFuture<ResponseEntity<Response>> logout(@RequestParam String token) {
//		CompletableFuture<Response> responseCompletableFuture = service.logout(refreshToken);
//		Response response = responseCompletableFuture.get(SERVICE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
//		return ResponseEntity.ok(response);
		return CompletableFuture.supplyAsync(() -> service.logout(token), executor)
			  .thenApply(ResponseEntity::ok)
			  .orTimeout(SERVICE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
	}
}
