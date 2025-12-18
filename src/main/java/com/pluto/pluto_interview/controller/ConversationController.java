package com.pluto.pluto_interview.controller;

import com.pluto.pluto_interview.model.Response;
import com.pluto.pluto_interview.model.dto.*;
import com.pluto.pluto_interview.service.ConversationService;
import com.pluto.pluto_interview.util.UserIdUtil;
import io.reactivex.Completable;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@RestController
@RequestMapping("/chat/conversations")
@RequiredArgsConstructor
public class ConversationController {
	private final ConversationService conversationService;
	private final Long SSE_EMITTER_TIMEOUT = 5 * 60 * 1000L; // 5 minutes in milliseconds
	private final Integer SERVICE_TIMEOUT = 30; // in seconds

	@GetMapping
	public ResponseEntity<Response> getConversations(
		  @RequestParam(name = "offset", required = false, defaultValue = "0") Integer offset,
		  @RequestParam(name = "limit", required = false, defaultValue = "10") Integer limit,
		  @RequestParam(name = "query", required = false) String query) throws ExecutionException,
		  InterruptedException, TimeoutException {
		CompletableFuture<Response> responseCompletableFuture = conversationService.getConversations(offset, limit, query);
		Response response = responseCompletableFuture.get(SERVICE_TIMEOUT, TimeUnit.SECONDS);
		return ResponseEntity.ok(response);
	}

	@PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter startConversation(@Valid @RequestBody StartConversationDto startConversationDto) {
		final SseEmitter emitter = new SseEmitter(SSE_EMITTER_TIMEOUT);
		Long userId = UserIdUtil.getUserId();
		conversationService.startConversation(userId, startConversationDto.getMessage(), emitter);
		return emitter;
	}

	@DeleteMapping
	public ResponseEntity<Response> deleteConversation(@Valid @RequestBody DeleteConversationDto dto)
		  throws ExecutionException, InterruptedException, TimeoutException {
		CompletableFuture<Response> responseFuture = new CompletableFuture<>();
		conversationService.deleteConversation(dto, responseFuture);
		Response response = responseFuture.get(SERVICE_TIMEOUT, TimeUnit.MINUTES);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/messages")
	public ResponseEntity<Response> getMessages(@Valid @RequestBody GetConversationsMessagesDto dto)
		  throws ExecutionException, InterruptedException, TimeoutException {
		CompletableFuture<Response> responseFuture = new CompletableFuture<>();
		conversationService.getMessages(dto, responseFuture);
		Response response = responseFuture.get(SERVICE_TIMEOUT, TimeUnit.MINUTES);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/messages")
	public SseEmitter sendMessage(@Valid @RequestBody SendMessageDto sendMessageDto) {
		final SseEmitter emitter = new SseEmitter(SSE_EMITTER_TIMEOUT);
		conversationService.sendMessage(emitter, sendMessageDto);
		return emitter;
	}
}
