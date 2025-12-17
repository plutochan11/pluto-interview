package com.pluto.pluto_interview.handler;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.output.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
public class SseEmitterHandler implements StreamingChatResponseHandler {
	private final SseEmitter emitter;
	private final CompletableFuture<String> responseFuture;

	@Override
	public void onPartialResponse(String partialResponse) {
		// If the future is already completed or failed, ignore further tokens.
		if (responseFuture.isDone()) {
			return;
		}

		try {
			emitter.send(SseEmitter.event().name("token")
				  .data(partialResponse));
		} catch (IOException e) {
			log.error("Error sending SSE event", e);
			responseFuture.completeExceptionally(e);
		}
	}

	@Override
	public void onCompleteResponse(ChatResponse chatResponse) {
		AiMessage aiMessage = chatResponse.aiMessage();
		if (aiMessage == null || aiMessage.text().isBlank()) {
			log.error("Error streaming AI response: AI message is null");
			responseFuture.completeExceptionally(new RuntimeException("AI message is null"));
			return;
		}
		String response = aiMessage.text();
		responseFuture.complete(response);
	}

	@Override
	public void onError(Throwable throwable) {
		responseFuture.completeExceptionally(throwable);
	}
}
