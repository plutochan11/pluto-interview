package com.pluto.pluto_interview.controller;

import com.pluto.pluto_interview.model.Response;
import com.pluto.pluto_interview.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.concurrent.*;

@RestController
@RequestMapping("/users")
public class UserController {
	private final UserService userService;
	private final Duration SERVICE_TIMEOUT ;
	private final TimeUnit SERVICE_TIMEOUT_UNIT = TimeUnit.SECONDS;
	private final Executor executor;

	public UserController(UserService userService, @Value("${service.user.timeout:20s}") Duration serviceTimeout, Executor executor) {
		this.userService = userService;
		SERVICE_TIMEOUT = serviceTimeout;
		this.executor = executor;
	}

	@GetMapping
	public ResponseEntity<Response> getUser() throws ExecutionException, InterruptedException, TimeoutException {
		CompletableFuture<Response> responseCompletableFuture = userService.getUser();
		Response response = responseCompletableFuture.get(SERVICE_TIMEOUT.toSeconds(), TimeUnit.SECONDS);
		return ResponseEntity.ok(response);
	}

	@DeleteMapping
	public CompletableFuture<ResponseEntity<Response>> deleteUser() {
		return CompletableFuture.supplyAsync(userService::deleteUser, executor)
			  .thenApply(ResponseEntity::ok)
			  .orTimeout(SERVICE_TIMEOUT.toSeconds(), SERVICE_TIMEOUT_UNIT);
	}
}
