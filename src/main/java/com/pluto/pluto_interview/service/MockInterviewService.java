package com.pluto.pluto_interview.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pluto.pluto_interview.enums.ErrorMessage;
import com.pluto.pluto_interview.exception.MockInterviewSessionNotFoundException;
import com.pluto.pluto_interview.exception.MockInterviewSessionOwnershipException;
import com.pluto.pluto_interview.exception.UserNotFoundException;
import com.pluto.pluto_interview.handler.SseEmitterHandler;
import com.pluto.pluto_interview.mapper.MockInterviewResultMapper;
import com.pluto.pluto_interview.model.*;
import com.pluto.pluto_interview.model.dto.*;
import com.pluto.pluto_interview.model.vo.*;
import com.pluto.pluto_interview.repository.MockInterviewRepository;
import com.pluto.pluto_interview.repository.QuestionRepository;
import com.pluto.pluto_interview.repository.UserRepository;
import com.pluto.pluto_interview.util.UserIdUtil;
import dev.langchain4j.data.message.*;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MockInterviewService {
	private final MockInterviewRepository mockInterviewRepository;
	private final UserRepository userRepo;
	private final QuestionRepository questionRepository;
	private final MockInterviewChatService chatService;
	private final ObjectMapper objectMapper;
	private final int STREAMING_CHAT_TIMEOUT = 5;
	private final MockInterviewEvaluationService evaluationService;
	private final MockInterviewResultMapper resultMapper;
	private final MockInterviewMessageService messageService;
	private final ChatMemoryProvider chatMemoryProvider;

	@Async
	public CompletableFuture<Response> getMockInterviewSessions(Integer offset, Integer limit) {
		// Create Pageable from offset and limit
		Pageable pageable = PageRequest.of(offset, limit, Sort.Direction.DESC, "updatedAt");

		// Fetch sessions by user ID
		Long userId = UserIdUtil.getUserId();
		Page<MockInterviewSession> sessionPage = mockInterviewRepository.findByCandidateId(userId, pageable);
		List<MockInterviewSession> sessions = sessionPage.getContent();

		// Create VO
		List<MockInterviewSessionVo> sessionVos = sessions.stream()
			  .map(s -> new MockInterviewSessionVo(s.getId(), s.getStatusAsString(), s.getUpdatedAt().toString()))
			  .toList();
		Pagination pagination = Pagination.builder()
			  .total(sessionPage.getTotalElements())
			  .pageCount(sessionPage.getTotalPages())
			  .offset(offset)
			  .limit(limit)
			  .build();
		GetMockInterviewSessionsVo vo = new GetMockInterviewSessionsVo(sessionVos, pagination);

		// Return the completed future
		return CompletableFuture.completedFuture(Response.ok(vo));
	}

	@Async
	@Transactional
	public void startMockInterviewSession(SseEmitter emitter) throws Throwable {
		// Get user's preferences of question types and difficulty levels
		Long userId = UserIdUtil.getUserId();
		User user = userRepo.findById(userId)
			  .orElseThrow(() -> new UserNotFoundException(ErrorMessage.USER_NOT_FOUND.getErrorMessage()));

		Settings settings = user.getSettings();
		List<String> preferredQuestionTypes = settings.getPreferredQuestionTypes();
		String preferredDifficultyLevel = settings.getPreferredDifficultyLevel();

		// Get relevant questions
		Specification<Question> spec = Specification.unrestricted();
		spec = spec.and((root, query, builder) ->
				                  builder.equal(root.get("difficultyLevel"), preferredDifficultyLevel))
			               .and((root, query, builder) -> root.get("questionType").in(preferredQuestionTypes));
		List<Question> questions = questionRepository.findAll(spec);

		// Assemble LLM context
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("""
			  You are a professional and helpful mock interview assistant and you are now holding a mock interview.
			  Only ask questions, and don't provide any additional information. However you can say some opening remarks such as "Welcome to the mock interview..." and encouraging words
			  
			  Below is the interview question bank for reference.
			  However you need to make up a question based on the user's preferred question types and difficulty level whether or not there are matching questions in the question bank.
			  
			  """);
		try {
			stringBuilder.append("Preferred question types: ")
				  .append(objectMapper.writeValueAsString(preferredQuestionTypes))
				  .append("\nPreferred difficulty level: ")
				  .append(preferredDifficultyLevel)
				  .append("\n\nQuestion bank: ")
				  .append(objectMapper.writeValueAsString(questions));
			String context = stringBuilder.toString();
			List<ChatMessage> chatMessages = List.of(
				  SystemMessage.from(context), UserMessage.from("Ask an interview question based on the user's preferred question types and difficulty level.")
			);

			// Stream the LLM response back to the client and get the full AI response
			CompletableFuture<String> aiResponseFuture = new CompletableFuture<>();
			StreamingChatResponseHandler responseHandler = new SseEmitterHandler(emitter, aiResponseFuture);
			chatService.mockInterview(chatMessages, responseHandler);
			String aiResponse = aiResponseFuture.get(STREAMING_CHAT_TIMEOUT, TimeUnit.MINUTES);

			// Persist the mock interview session
			MockInterviewMessage message = MockInterviewMessage.fromInterviewer(aiResponse);
			List<MockInterviewMessage> messages = List.of(
				  message
			);

			MockInterviewSession session = MockInterviewSession.newSession();
			session.setCandidate(user);
			session.addMessages(messages);

			message.setSession(session);

			MockInterviewSession saved = mockInterviewRepository.save(session);

			// Send done event and complete emitter
			Map<String, Long> doneEventValue = Map.of(
				  "sessionId", saved.getId()
			);
			emitter.send(SseEmitter.event()
				  .name("done")
				  .data(objectMapper.writeValueAsString(doneEventValue)));
			emitter.complete();
		} catch (JsonProcessingException e) {
			emitter.completeWithError(e);
			log.error("Error serializing preferred question types: {}", e.getMessage());
			return;
		} catch (ExecutionException e) {
			Throwable cause = e.getCause() != null ? e.getCause() : e;
			emitter.completeWithError(cause);
			log.error("Chat execution failed: {}", cause.getMessage(), cause);
			throw cause;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			emitter.completeWithError(e);
			log.error("Chat thread interrupted: {}", e.getMessage());
			return;
		} catch (TimeoutException e) {
			emitter.completeWithError(e);
			log.error("Chat timed out after {} minutes", STREAMING_CHAT_TIMEOUT);
			return;
		} catch (IOException e) {
			emitter.completeWithError(e);
			log.error("Failed to send done event: {}", e.getMessage());
		}
	}

	@Async
	public CompletableFuture<Response> getMessages(Long sessionId, Integer offset, Integer limit) {
		// Create Pageable as per offset and limit
		Pageable pageable = PageRequest.of(offset, limit, Sort.Direction.DESC, "createdAt");

		// Get messages by session ID
		Page<MockInterviewMessage> messagePage = messageService.getMessages(sessionId, pageable);
		List<MockInterviewMessage> messages = messagePage.getContent();

		// Create result and return completed future with it
		List<MockInterviewMessageVo> messageVos = messages.stream()
			  .map(m -> new MockInterviewMessageVo(
				    m.getContent(),
				    m.getRoleAsString(),
				    m.getCreatedAt().toString()
			  ))
			  .toList();
		Pagination pagination = new Pagination(messagePage.getTotalElements(), messagePage.getTotalPages(),
			  offset, limit);
		GetMockInterviewMessagesResult result = new GetMockInterviewMessagesResult(messageVos, pagination);
		return CompletableFuture.completedFuture(Response.ok(result));
	}

	@Async
	@Transactional
	public void answerMockInterviewSession(AnswerMockInterviewSessionDto dto, SseEmitter emitter) throws Throwable {
		// Find session info by ID
		MockInterviewSession session = mockInterviewRepository.findById(dto.sessionId())
			  .orElseThrow(() -> new MockInterviewSessionNotFoundException(
				    ErrorMessage.MOCK_INTERVIEW_SESSION_NOT_FOUND.getErrorMessage()));

		// Add answer to the session
		session.addMessages(MockInterviewMessage.fromCandidate(dto.answer()));

		Map<String, Boolean> doneEventValue;
		if (session.getMessages().size() >= 10) {
			session.setStatus(MockInterviewSession.Status.COMPLETED);
			mockInterviewRepository.save(session);
			doneEventValue = Map.of(
				  "continueInterview", false
			);
			emitter.send(SseEmitter.event()
				  .name("done")
				  .data(doneEventValue));
			emitter.complete();
			return;
		} else {
			doneEventValue = Map.of(
				  "continueInterview", true
			);
		}

		// Assemble the chat memory manually with all previous messages
		List<com.pluto.pluto_interview.model.dto.ChatMessage> chatMessages = session.getMessages().stream()
			  .map(message -> {
				  return com.pluto.pluto_interview.model.dto.ChatMessage.builder()
					    .content(message.getContent())
					    .role(message.getRoleAsString())
					    .build();
			  })
			  .toList();
		String systemMessage = """
			  You are a professional mock interviewer and you are now holding a mock interview.
			  You need to ask the next interview question based on the chat history.
			  However, you don't have to ask follow-up questions about the previous question. It's up to you to switch to a new topic or stick with the previous question.
			  Make sure the next question is different from previous questions.
			  """;
		StringBuilder stringBuilder = new StringBuilder();
		try {
			stringBuilder.append("Here are chat history: ")
				  .append(objectMapper.writeValueAsString(chatMessages));
		} catch (JsonProcessingException e) {
			emitter.completeWithError(e);
			log.error("Error serializing chat messages: {}", e.getMessage());
			return;
		}
		stringBuilder.append("\nHere is the answer to the previous question: ")
			  .append(dto.answer());
		String userMessage = stringBuilder.toString();
		List<ChatMessage> llmChatMessages = List.of(
			  SystemMessage.from(systemMessage),
			  UserMessage.from(userMessage)
		);

		// Interact with the LLM and retrieve the full AI response
		CompletableFuture<String> aiResponseFuture = new CompletableFuture<>();
		StreamingChatResponseHandler responseHandler = new SseEmitterHandler(emitter, aiResponseFuture);
		chatService.mockInterview(llmChatMessages, responseHandler);

		String aiResponse = null;
		try {
			aiResponse = aiResponseFuture.get(STREAMING_CHAT_TIMEOUT, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			emitter.completeWithError(e);
			log.error("Chat thread interrupted: {}", e.getMessage());
			return;
		} catch (ExecutionException e) {
			Throwable cause = e.getCause() != null ? e.getCause() : e;
			emitter.completeWithError(cause);
			log.error("Chat execution failed: {}", cause.getMessage(), cause);
			throw cause;
		} catch (TimeoutException e) {
			emitter.completeWithError(e);
			log.error("Chat timed out after {} minutes", STREAMING_CHAT_TIMEOUT);
			return;
		}

		// Add response to the session
		session.addMessages(MockInterviewMessage.fromInterviewer(aiResponse));

		// Persist the session, send the done event, and complete the emitter
		MockInterviewSession save = mockInterviewRepository.save(session);

		emitter.send(SseEmitter.event()
			  .name("done")
			  .data(objectMapper.writeValueAsString(doneEventValue)));
		emitter.complete();
	}

	@Transactional
	public void endMockInterviewSession(EndMockInterviewSessionDto dto) {
		// Find the session by ID
		MockInterviewSession session = findSessionAndVerifyOwnership(dto.sessionId());

		// Set the status to CANCELLED
		session.setStatus(MockInterviewSession.Status.CANCELLED);

		// Save the session
		mockInterviewRepository.save(session);
	}

	@Async
	public CompletableFuture<Response> getMockInterviewResult(Long sessionId) {
		// Find session by ID and verify authority
		MockInterviewSession session = mockInterviewRepository.findById(sessionId)
			  .orElseThrow(() -> new MockInterviewSessionNotFoundException(
			      ErrorMessage.MOCK_INTERVIEW_SESSION_NOT_FOUND.getErrorMessage()));

		Long userId = UserIdUtil.getUserId();
		if (!Objects.equals(userId, session.getCandidate().getId())) {
			throw new MockInterviewSessionOwnershipException(
				  ErrorMessage.MOCK_INTERVIEW_SESSION_OWNERSHIP_VIOLATION.getErrorMessage());
		}

		// Validate status
		if (session.getStatus() != MockInterviewSession.Status.COMPLETED) {
			throw new IllegalStateException("Mock interview session is not completed yet.");
		}

		ChatMemory chatMemory = chatMemoryProvider.get(session.getId());

		// Convert chat messages to a formatted string with roles.
		String chatHistory = chatMemory.messages().stream()
			  .map(this::formatChatMessage)
			  .collect(Collectors.joining("\n\n"));

		// Get evaluation result, persist session and map result to VO
		MockInterviewResult result = evaluationService.evaluate(chatHistory);
		result.setSession(session);

		session.setResult(result);
		mockInterviewRepository.save(session);

		MockInterviewResultVo vo = resultMapper.toVo(result);
		return CompletableFuture.completedFuture(Response.ok(vo));
	}

	private MockInterviewSession findSessionAndVerifyOwnership(Long sessionId) {
		MockInterviewSession session = mockInterviewRepository.findById(sessionId)
			  .orElseThrow(() -> new MockInterviewSessionNotFoundException(
				    ErrorMessage.MOCK_INTERVIEW_SESSION_NOT_FOUND.getErrorMessage()));
		Long userId = UserIdUtil.getUserId();
		if (!Objects.equals(userId, session.getCandidate().getId())) {
			throw new MockInterviewSessionOwnershipException(
				  ErrorMessage.MOCK_INTERVIEW_SESSION_OWNERSHIP_VIOLATION.getErrorMessage());
		}
		return session;
	}

	private String formatChatMessage(ChatMessage chatMessage) {
		if (chatMessage instanceof UserMessage) {
			StringBuilder stringBuilder = new StringBuilder();
			((UserMessage) chatMessage).contents().forEach(content -> {
				if (content instanceof TextContent) {
					stringBuilder.append(((TextContent) content).text())
						  .append("\n");
				}
			});
			return String.format("Candidate: %s", stringBuilder.toString().trim());
		} else if (chatMessage instanceof AiMessage) {
			return String.format("Interviewer: %s", ((AiMessage) chatMessage).text());
		}
		return "";
	}
}
