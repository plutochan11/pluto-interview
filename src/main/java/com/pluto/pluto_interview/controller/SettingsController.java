package com.pluto.pluto_interview.controller;

import com.pluto.pluto_interview.model.Response;
import com.pluto.pluto_interview.model.dto.NewSettings;
import com.pluto.pluto_interview.service.SettingsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/settings")
public class SettingsController {
	private final SettingsService settingsService;
	private final Executor executor;
	private final Duration SERVICE_TIMEOUT;

	private static final TimeUnit SERVICE_TIMEOUT_UNIT = TimeUnit.SECONDS;

	public SettingsController(SettingsService settingsService, Executor executor,
	                          @Value("${service.settings.timeout:5s}") Duration serviceTimeout) {
		this.settingsService = settingsService;
		this.executor = executor;
		SERVICE_TIMEOUT = serviceTimeout;
	}

	@GetMapping
	public CompletableFuture<ResponseEntity<Response>> getSettings() {
		return CompletableFuture.supplyAsync(settingsService::getSettings, executor)
			  .thenApply(ResponseEntity::ok)
			  .orTimeout(SERVICE_TIMEOUT.toSeconds(), SERVICE_TIMEOUT_UNIT);
	}

	@PatchMapping
	public CompletableFuture<ResponseEntity<Response>> updateSettings(@Valid @RequestBody NewSettings newSettings) {
		return CompletableFuture.supplyAsync(() -> settingsService.updateSettings(newSettings), executor)
			  .thenApply(ResponseEntity::ok)
			  .orTimeout(SERVICE_TIMEOUT.toSeconds(), SERVICE_TIMEOUT_UNIT);
	}
}
