package com.pluto.pluto_interview.service;

import com.pluto.pluto_interview.handler.SseEmitterHandler;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class MockInterviewChatService {
	private final StreamingChatModel streamingChatModel;

	@Async
	public void mockInterview(List<ChatMessage> chatMessages, StreamingChatResponseHandler responseHandler) {
		streamingChatModel.chat(chatMessages, responseHandler);
	}

	@Async
	public void evaluateMockInterview(List<ChatMessage> chatMessages, StreamingChatResponseHandler responseHandler) {
		streamingChatModel.chat(chatMessages, responseHandler);
	}
}
