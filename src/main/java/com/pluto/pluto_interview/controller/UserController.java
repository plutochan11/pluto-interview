package com.pluto.pluto_interview.controller;

import com.pluto.pluto_interview.model.Response;
import com.pluto.pluto_interview.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
	private final UserService service;
	private final int SERVICE_TIMEOUT_SECONDS = 10;

	@GetMapping
	public ResponseEntity<Response> getUser() throws ExecutionException, InterruptedException, TimeoutException {
		CompletableFuture<Response> responseCompletableFuture = service.getUser();
		Response response = responseCompletableFuture.get(SERVICE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		return ResponseEntity.ok(response);
	}
}
