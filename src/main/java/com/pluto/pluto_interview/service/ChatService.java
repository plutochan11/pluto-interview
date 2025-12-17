package com.pluto.pluto_interview.service;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class ChatService {
	private final StreamingChatModel streamingChatModel;
	private final ChatModel chatModel;

	public void streamChat(String userMessage, StreamingChatResponseHandler streamingResponseHandler) {
		List<ChatMessage> chatMessages = List.of(
			  SystemMessage.from("""
				    You are a professional and helpful interview assistant.
				    Help the user to prepare for interviews by providing detailed
				    and informative answers and try to answer the question as if
				    you are in an interview so that you can provide the user with a template
				    to answer this question in an interview.
				    """),
			  UserMessage.from(userMessage)
		);
		streamingChatModel.chat(userMessage, streamingResponseHandler);
	}

	@Async
	public CompletableFuture<String> generateTitle(String userMessage) {
		String prompt = String.format("Generate a concise title" +
			  " no more than 5 words and ONLY response this title" +
			  " for the following user query: \"%s\"", userMessage);
		String title = chatModel.chat(prompt);
		return CompletableFuture.completedFuture(title);
	}
}
