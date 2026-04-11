package com.pluto.pluto_interview.controller;

import com.pluto.pluto_interview.model.Response;
import com.pluto.pluto_interview.model.dto.AnswerMockInterviewSessionDto;
import com.pluto.pluto_interview.model.dto.EndMockInterviewSessionDto;
import com.pluto.pluto_interview.service.MockInterviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


@RestController
@RequestMapping("/mock-interview/sessions")
@RequiredArgsConstructor
public class MockInterviewController {
	private final long SSE_EMITTER_TIMEOUT = 5 * 60 * 1000L; // 5 minutes in milliseconds
	private final MockInterviewService mockInterviewService;
	private final int SERVICE_TIMEOUT = 30; // seconds

	@GetMapping
	public ResponseEntity<Response> getSessionsByPagination(
		  @RequestParam(name = "offset", required = false, defaultValue = "0") Integer offset,
		  @RequestParam(name = "limit", required = false, defaultValue = "10") Integer limit)
	{

		CompletableFuture<Response> responseCompletableFuture = mockInterviewService.getMockInterviewSessions(offset, limit);

		Response response = null;
		try {
			response = responseCompletableFuture.get(SERVICE_TIMEOUT, TimeUnit.SECONDS);
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

	@PostMapping
	public SseEmitter startNewSession() throws Throwable {

		SseEmitter emitter = new SseEmitter(SSE_EMITTER_TIMEOUT);
		mockInterviewService.startMockInterviewSession(emitter);
		return emitter;
	}

	@GetMapping("/{sessionId}/messages")
	public ResponseEntity<Response> getMessagesOfSession(
		  @PathVariable(name = "sessionId") Long sessionId,
		  @RequestParam(name = "offset", required = false, defaultValue = "0") Integer offset,
		  @RequestParam(name = "limit", required = false, defaultValue = "10") Integer limit)
	{

		CompletableFuture<Response> responseCompletableFuture = mockInterviewService.getMessages(sessionId, offset, limit);

		Response response = null;
		try {
			response = responseCompletableFuture.get(SERVICE_TIMEOUT, TimeUnit.SECONDS);
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

	@PostMapping("/answer")
	public SseEmitter answerToSession(@Valid @RequestBody AnswerMockInterviewSessionDto dto) throws Throwable {

		// Create a SseEmitter with a timeout and pass it to the service layer
		SseEmitter emitter = new SseEmitter(SSE_EMITTER_TIMEOUT);
		mockInterviewService.answerMockInterviewSession(dto, emitter);

		// Return the SseEmitter
		return emitter;
	}

	@PatchMapping("/end")
	public ResponseEntity<Response> endSession(@RequestBody @Valid EndMockInterviewSessionDto dto){

		mockInterviewService.endMockInterviewSession(dto);
		return ResponseEntity.ok(Response.ok());
	}

	@GetMapping("/{sessionId}/result")
	public ResponseEntity<Response> getResult(@PathVariable Long sessionId) {

		CompletableFuture<Response> responseCompletableFuture = mockInterviewService.getMockInterviewResult(sessionId);

		Response response = null;
		try {
			response = responseCompletableFuture.get(SERVICE_TIMEOUT, TimeUnit.MINUTES);
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
}
