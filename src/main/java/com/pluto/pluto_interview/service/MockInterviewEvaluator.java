package com.pluto.pluto_interview.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pluto.pluto_interview.model.MockInterviewResult;
import com.pluto.pluto_interview.model.dto.MockInterviewResultDto;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.request.ResponseFormatType;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonSchema;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MockInterviewEvaluator {
	private final ChatModel chatModel;
	private final ObjectMapper objectMapper;

	@SneakyThrows
	public MockInterviewResultDto evaluate(String interviewHistory) {
		// Create chat messages
		String systemMessageString = """
			  You are an expert mock interview evaluator who is good at evaluate a mock interview based on given interview history.
			  Provide a score (0-10), count the question answered, and list strengths, improvements and overall feedback.
			  You MUST strictly return your evaluation in a JSON manner matching this structure:
			          {
			             "score": 8.5,
			             "questionAnswered": 5,
			             "strengths": "Good knowledge of Java...",
			             "improvements": "Could improve on...",
			             "overallFeedback": "Strong candidate..."
			          }
			   ONLY return JSON, without any explanation, without surrounding markdown code markup.
			  """;
		SystemMessage systemMessage = SystemMessage.from(systemMessageString);
		UserMessage userMessage = UserMessage.from("Here is the interview history: \n" + interviewHistory);
		List<ChatMessage> chatMessages = List.of(systemMessage, userMessage);

		// Call
		String aiResponse = chatModel.chat(chatMessages).aiMessage().text();

		// Parse the JSON to DTO
		return objectMapper.readValue(aiResponse, MockInterviewResultDto.class);
	}
}
