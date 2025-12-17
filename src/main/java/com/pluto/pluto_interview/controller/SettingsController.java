package com.pluto.pluto_interview.controller;

import com.pluto.pluto_interview.model.Response;
import com.pluto.pluto_interview.model.dto.SettingsDto;
import com.pluto.pluto_interview.service.SettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@RestController
@RequiredArgsConstructor
public class SettingsController {
	private final SettingsService settingsService;
	private final Executor executor;

	@GetMapping("/settings")
	public ResponseEntity<Response> getSettings() {
		Response response = settingsService.getSettings();
		return ResponseEntity.ok(response);
	}

	@PatchMapping("/settings")
	public ResponseEntity<Response> adjustSettings(@Valid @RequestBody SettingsDto settingsDto) {
//		return CompletableFuture.supplyAsync(() -> {
//			Response response = settingsService.adjustSettings(settingsDto);
//			return ResponseEntity.ok(response);
//		}, executor);
		Response response = settingsService.adjustSettings(settingsDto);
		return ResponseEntity.ok(response);
	}
}
