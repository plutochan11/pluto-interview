package com.pluto.pluto_interview.controller;

import com.pluto.pluto_interview.model.Response;
import com.pluto.pluto_interview.model.dto.AnswerMockInterviewSessionDto;
import com.pluto.pluto_interview.model.dto.EndMockInterviewSessionDto;
import com.pluto.pluto_interview.model.dto.GetMockInterviewResultDto;
import com.pluto.pluto_interview.model.dto.GetMockInterviewSessionsDto;
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
	public ResponseEntity<Response> getMockInterviewSessions(
		  @RequestParam(name = "offset", required = false, defaultValue = "0") Integer offset,
		  @RequestParam(name = "limit", required = false, defaultValue = "10") Integer limit)
		  throws ExecutionException, InterruptedException, TimeoutException {
		CompletableFuture<Response> responseCompletableFuture = mockInterviewService.getMockInterviewSessions(offset, limit);
		Response response = responseCompletableFuture.get(SERVICE_TIMEOUT, TimeUnit.SECONDS);
		return ResponseEntity.ok(response);
	}

	@PostMapping
	public SseEmitter startMockInterviewSession() throws Throwable {
		SseEmitter emitter = new SseEmitter(SSE_EMITTER_TIMEOUT);
		mockInterviewService.startMockInterviewSession(emitter);
		return emitter;
	}

	@GetMapping("/{sessionId}/messages")
	public ResponseEntity<Response> getMessages(
		  @PathVariable(name = "sessionId") Long sessionId,
		  @RequestParam(name = "offset", required = false, defaultValue = "0") Integer offset,
		  @RequestParam(name = "limit", required = false, defaultValue = "10") Integer limit)
		  throws ExecutionException, InterruptedException, TimeoutException {
		CompletableFuture<Response> responseCompletableFuture = mockInterviewService.getMessages(sessionId, offset, limit);
		Response response = responseCompletableFuture.get(SERVICE_TIMEOUT, TimeUnit.SECONDS);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/answer")
	public SseEmitter answerMockInterviewSession(@Valid @RequestBody AnswerMockInterviewSessionDto dto) throws Throwable {
		// Create a SseEmitter with a timeout and pass it to the service layer
		SseEmitter emitter = new SseEmitter(SSE_EMITTER_TIMEOUT);
		mockInterviewService.answerMockInterviewSession(dto, emitter);

		// Return the SseEmitter
		return emitter;
	}

	@PatchMapping("/end")
	public ResponseEntity<Response> endMockInterviewSession(@RequestBody @Valid EndMockInterviewSessionDto dto)
		  throws ExecutionException, InterruptedException, TimeoutException {
		mockInterviewService.endMockInterviewSession(dto);
		return ResponseEntity.ok(Response.ok());
	}

//	@GetMapping("/result")
//	public SseEmitter getMockInterviewResult(@Valid @RequestBody GetMockInterviewResultDto dto) throws Throwable {
//		// Create a SseEmitter with a timeout and pass it to the service layer
//		SseEmitter emitter = new SseEmitter(SSE_EMITTER_TIMEOUT);
//		mockInterviewService.getMockInterviewResult(dto, emitter);
//
//		// Return the SseEmitter
//		return emitter;
//	}

	@GetMapping("/{sessionId}/result")
	public ResponseEntity<Response> getMockInterviewResult(@PathVariable Long sessionId)
		  throws ExecutionException, InterruptedException, TimeoutException {
		CompletableFuture<Response> responseCompletableFuture = mockInterviewService.getMockInterviewResult(sessionId);
		Response response = responseCompletableFuture.get(SERVICE_TIMEOUT, TimeUnit.MINUTES);
		return ResponseEntity.ok(response);
	}
}
