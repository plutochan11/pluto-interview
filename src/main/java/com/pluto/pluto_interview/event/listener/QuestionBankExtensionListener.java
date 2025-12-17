package com.pluto.pluto_interview.event.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pluto.pluto_interview.event.UserMessageResponsedEvent;
import com.pluto.pluto_interview.model.Message;
import com.pluto.pluto_interview.service.ChatService;
import com.pluto.pluto_interview.service.QuestionBankService;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class QuestionBankExtensionListener {
	private final QuestionBankService questionBankService;
	private final ChatModel chatModel;
	private final ObjectMapper objectMapper;

	@Async
//	@EventListener(UserMessageResponsedEvent.class)
	private void onUserMessageResponsedEvent(UserMessageResponsedEvent event) {
		// Assemble ChatMessage from the user message and the response
		List<Message> messages = List.copyOf(event.getMessages());
		Map<String, String> context = Map.of(
			  "Question", messages.getFirst().getContent(),
			  "Answer", messages.getLast().getContent()
		);
//		try {
//			String contextString = objectMapper.writeValueAsString(context);
//			List<ChatMessage> chatMessages = List.of(
//				  SystemMessage.from("""
//					    You are an expert at managing a question bank for interview preparation.
//					    Your task is to evaluate whether a given question and its answer should be added to the question bank.
//					    Consider the relevance, clarity, and quality of both the question and the answer.
//					    Respond with "Yes" if the question should be added to the question bank, otherwise respond with "No".
//					    """),
//				  UserMessage.from(contextString)
//			);
//			String decision = chatModel.chat(chatMessages).aiMessage().text();
//			switch (decision) {
//				case "Yes" -> questionBankService.addQuestion(messages.getFirst().getContent(),
//					  messages.getLast().getContent());
//				case "No" -> log.info("Question not added to the question bank based on evaluation.");
//				default -> log.warn("Unexpected response from chat model: {}", decision);
//			}
//		} catch (JsonProcessingException e) {
//			log.error("Error serialising context for question bank extension", e);
//			return;
//		}
	}
}
