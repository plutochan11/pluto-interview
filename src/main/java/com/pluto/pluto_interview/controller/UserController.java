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

	public UserController(
		  UserService userService,
		  @Value("${service.user.timeout:20s}") Duration serviceTimeout,
		  Executor executor) {

		this.userService = userService;
		SERVICE_TIMEOUT = serviceTimeout;
		this.executor = executor;
	}

	@GetMapping
	public ResponseEntity<Response> get() {

		CompletableFuture<Response> responseCompletableFuture = userService.getUser();

		Response response = null;
		try {
			response = responseCompletableFuture.get(SERVICE_TIMEOUT.toSeconds(), TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return ResponseEntity.status(500).body(
				  Response.error("Service interrupted. Please try again later."));
		} catch (ExecutionException e) {
			// Rethrow with cause.
			throw new RuntimeException(e.getCause());
		} catch (TimeoutException e) {
			return ResponseEntity.status(504).body(
				  Response.error("Timeout. Please try again later."));
		}

		return ResponseEntity.ok(response);
	}

	@DeleteMapping
	public CompletableFuture<ResponseEntity<Response>> delete() {

		return CompletableFuture.supplyAsync(userService::deleteUser, executor)
			  .thenApply(ResponseEntity::ok)
			  .orTimeout(SERVICE_TIMEOUT.toSeconds(), SERVICE_TIMEOUT_UNIT);
	}
}
