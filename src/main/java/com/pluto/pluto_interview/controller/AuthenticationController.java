package com.pluto.pluto_interview.controller;

import com.pluto.pluto_interview.enums.ErrorMessage;
import com.pluto.pluto_interview.model.Response;
import com.pluto.pluto_interview.model.dto.AuthenticationDto;
import com.pluto.pluto_interview.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthenticationController {
	private final AuthenticationService authService;
	private final Executor executor;

	@PostMapping("/register")
	public CompletableFuture<ResponseEntity<Response>> register(@Valid @RequestBody AuthenticationDto authDto) {
//		Response response = authService.register(authDto);
//		return ResponseEntity.status(HttpStatus.CREATED).body(response);

		return CompletableFuture.supplyAsync(() -> {
			Response response = authService.register(authDto);
			return ResponseEntity.status(HttpStatus.CREATED).body(response);
		}, executor);
	}

	@PostMapping("login")
	public ResponseEntity<Response> login(@Valid @RequestBody AuthenticationDto authDto) {
		Response response = authService.login(authDto);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/logout")
	public ResponseEntity<Response> logout(@RequestHeader(name = "Authorization") String authHeader) {
		// Verify if it's a bearer auth token
		if (!authHeader.startsWith("Bearer")) {
			return ResponseEntity.noContent().build();
		}

		// Extract the token
		String token = authHeader.substring(7);
		// Make the token invalid
		authService.logout(token);
		// Return
		Response response = Response.ok();
		return ResponseEntity.ok(response);
	}
}
