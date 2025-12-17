package com.pluto.pluto_interview.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pluto.pluto_interview.enums.ErrorMessage;
import com.pluto.pluto_interview.exception.ConversationNotFoundException;
import com.pluto.pluto_interview.exception.ConversationOwnershipException;
import com.pluto.pluto_interview.exception.UserNotFoundException;
import com.pluto.pluto_interview.handler.SseEmitterHandler;
import com.pluto.pluto_interview.model.Conversation;
import com.pluto.pluto_interview.model.Message;
import com.pluto.pluto_interview.model.Response;
import com.pluto.pluto_interview.model.User;
import com.pluto.pluto_interview.model.dto.*;
import com.pluto.pluto_interview.model.vo.ConversationVo;
import com.pluto.pluto_interview.model.vo.GetConversationsMessagesVo;
import com.pluto.pluto_interview.model.vo.GetConversationsVo;
import com.pluto.pluto_interview.model.vo.MessagesVo;
import com.pluto.pluto_interview.repository.ConversationRepository;
import com.pluto.pluto_interview.repository.UserRepository;
import com.pluto.pluto_interview.util.UserIdUtil;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConversationService {
	private final ConversationRepository conversationRepo;
	private final UserRepository userRepository;
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final ChatService chatService;
	private final Long STREAMING_TIMEOUT = 5L; // in minutes
	private final EmbeddingModel embeddingModel;
	private final EmbeddingStore<TextSegment> embeddingStore;

	@Async("taskExecutor")
	@Transactional
	public void startConversation(Long userId, String userMessage, SseEmitter emitter) {
		try {
			// Create new Conversation and Message entities and associate them
			Conversation conversation = new Conversation();
			Message message = Message.fromUser(userMessage);
			conversation.addMessage(message);

			// Associate the conversation with the user and generate a title based on the user message
			User user = userRepository.findById(userId).orElseThrow(() ->
				  new UserNotFoundException(ErrorMessage.USER_NOT_FOUND.getErrorMessage()));
			conversation.setUser(user);
			CompletableFuture<String> titleFuture = chatService.generateTitle(userMessage);

			// Convert user message to embedding
			Embedding queryEmbedding = embeddingModel.embed(userMessage).content();

			// Find relevant segments (e.g. top 3 matches)
			EmbeddingSearchRequest embeddingSearchRequest = EmbeddingSearchRequest.builder()
				  .queryEmbedding(queryEmbedding)
				  .maxResults(3)
				  .build();
			List<EmbeddingMatch<TextSegment>> relevantSegments = embeddingStore.search(embeddingSearchRequest).matches();

			// Construct the augmented prompt
			String augmentedMessage = userMessage;
			if (!relevantSegments.isEmpty()) {
				String context = relevantSegments.stream()
					  .map(match -> match.embedded().text())
					  .collect(Collectors.joining("\n"));

				augmentedMessage = "Context information is below:\n" + context +
					  "\n\nGiven the context information and not prior knowledge, answer the user question: " +
					  userMessage;
			}

			// Stream the AI response back to the client using the SseEmitter
			CompletableFuture<String> responseFuture = streamChat(augmentedMessage, emitter);
			CompletableFuture.allOf(responseFuture, titleFuture).get(STREAMING_TIMEOUT, TimeUnit.MINUTES);

			// Record the AI response as a Message entity and associate it with the Conversation
			String title = titleFuture.get();
			conversation.setTitle(title);

			String response = responseFuture.get();
			Message AiMessage = Message.fromAi(response);
			conversation.addMessage(AiMessage);

			// Persist the conversation and AI message
			Conversation saved = conversationRepo.save(conversation);

			// Record the conversation ID
			Long conversationId = saved.getId();

			// Send the done event containing the conversation ID and title
			sendDoneEvent(emitter, conversationId, title);
			emitter.complete();
		} catch (ExecutionException | InterruptedException | TimeoutException e) {
			log.error("Error during streaming response", e);
			emitter.completeWithError(e);
		} catch (JsonProcessingException e) {
			log.error("Error serializing done data: {}", e.getMessage());
			emitter.completeWithError(e);
		} catch (IOException e) {
			log.error("Error sending done event: {}", e.getMessage());
			emitter.completeWithError(e);
		} catch (Exception e) {
			emitter.completeWithError(e);
		}
	}

	@Async
	@Transactional
	public void sendMessage(SseEmitter emitter, SendMessageDto sendMessageDto) {
		try {
			// Fetch Conversation record and add new user message
			Conversation conversation = conversationRepo.findById(Long.valueOf(sendMessageDto.conversationId()))
				  .orElseThrow(() -> new ConversationNotFoundException(ErrorMessage.CONVERSATION_NOT_FOUND.getErrorMessage()));
			conversation.addMessage(Message.fromUser(sendMessageDto.message()));
			// Stream return AI response
			CompletableFuture<String> responseFuture = streamChat(sendMessageDto.message(), emitter);

			// Record full AI response and add to Conversation
			String aiResponse = responseFuture.get(STREAMING_TIMEOUT, TimeUnit.MINUTES);
			conversation.addMessage(Message.fromAi(aiResponse));

			// Persist Conversation
			conversationRepo.save(conversation);
			emitter.complete();
		} catch (ConversationNotFoundException e) {
			onError(emitter, ErrorMessage.CONVERSATION_NOT_FOUND.getErrorMessage(), e);

			Long userId = UserIdUtil.getUserId();
			log.error("User(ID: {}) attempted to send message to non-existent conversation(ID: {})",
				  userId, sendMessageDto.conversationId());
		} catch (NumberFormatException e) {
			onError(emitter, ErrorMessage.INVALID_CONVERSATION_FORMAT.getErrorMessage(), e);

			log.error("User(ID: {}) provided invalid conversation ID format({})",
				  UserIdUtil.getUserId(), sendMessageDto.conversationId());
		} catch (ExecutionException | InterruptedException e) {
			sendErrorEvent(emitter, ErrorMessage.UNEXPECTED_ERROR_HAPPENED.getErrorMessage());
			emitter.completeWithError(e);

			log.error("Error during streaming response for user(ID: {}) in conversation(ID: {})",
				  UserIdUtil.getUserId(), sendMessageDto.conversationId(), e);
		} catch (TimeoutException e) {
			onError(emitter, ErrorMessage.STREAMING_RESPONSE_TIMEOUT.getErrorMessage(), e);

			log.error("Streaming response timeout for user(ID: {}) in conversation(ID: {})",
				  UserIdUtil.getUserId(), sendMessageDto.conversationId(), e);
		}
	}

	@Transactional
	public void getMessages(GetConversationsMessagesDto dto, CompletableFuture<Response> responseFuture) {
		Long userId = UserIdUtil.getUserId();
		try {
			// Verify the conversation existence whether it belongs to the user
			Conversation conversation = conversationRepo.findById(Long.valueOf(dto.conversationId()))
				  .orElseThrow(() -> new ConversationNotFoundException(ErrorMessage.CONVERSATION_NOT_FOUND.getErrorMessage()));
			if (!Objects.equals(userId, conversation.getUser().getId())) {
				throw new ConversationOwnershipException(ErrorMessage.NOT_YOUR_CONVERSATION.getErrorMessage());
			}
			// Assemble GetConversationsMessagesVo
			GetConversationsMessagesVo vo = assembleGetConversationsMessagesVo(dto, conversation);

			// Complete the future
			Response response = Response.ok(vo);
			responseFuture.complete(response);
		} catch (ConversationNotFoundException e) {
			log.error("User(ID: {}) attempted to get messages from non-existent conversation(ID: {})",
				  userId, dto.conversationId(), e);
			responseFuture.completeExceptionally(e);
		} catch (NumberFormatException e) {
			log.error("User(ID: {}) provided invalid conversation ID format({})",
				  userId, dto.conversationId(), e);
			responseFuture.completeExceptionally(e);
		}
	}

	@Async
	public CompletableFuture<Response> getConversations(Integer offset, Integer limit,
	                                                    String query) {
		// Get user ID and fetch conversations
		Long userId = UserIdUtil.getUserId();
		Pageable pageable = PageRequest.of(offset, limit,
			  Sort.by(Sort.Direction.DESC, "updatedAt"));
		Page<Conversation> conversationPage = query != null
			  ? conversationRepo.findByUserIdAndQuery(userId, query, pageable)
			  : conversationRepo.findByUserId(userId, pageable);

		// Log
		log.info("User({}) queries conversations with parameters: offset({}), limit({}), query({})",
			  userId, offset, limit, query);

		// Construct VO and Pagination
		List<ConversationVo> conversationVos = conversationPage.getContent().stream()
			  .map(c -> new ConversationVo(c.getId(), c.getTitle()))
			  .toList();
		Pagination pagination = new Pagination(conversationPage.getTotalElements(),
			  conversationPage.getTotalPages(), offset, limit);

		// Assemble VO
		GetConversationsVo vo = new GetConversationsVo(conversationVos, pagination);

		// Complete future
		return CompletableFuture.completedFuture(Response.ok(vo));
	}

	public void deleteConversation(@Valid DeleteConversationDto dto, CompletableFuture<Response> responseFuture) {
		// Fetch the conversation and see if it belongs to the user
		Long userId = UserIdUtil.getUserId();
		Conversation conversation = conversationRepo.findById(dto.conversationId())
			  .orElseThrow(() -> new ConversationNotFoundException(
				    ErrorMessage.CONVERSATION_NOT_FOUND.getErrorMessage()
			  ));
		if (!Objects.equals(userId, conversation.getUser().getId())) {
			throw new ConversationOwnershipException(
				  ErrorMessage.NOT_YOUR_CONVERSATION.getErrorMessage()
			);
		}

		// Delete conversation
		conversationRepo.delete(conversation);

		// Assemble response and complete future
		responseFuture.complete(Response.ok());
	}

	/**
	 * Assemble GetConversationsMessagesVo from Conversation entity and DTO
	 * @param dto
	 * @param conversation
	 * @return
	 */
	@NotNull
	private static GetConversationsMessagesVo assembleGetConversationsMessagesVo(GetConversationsMessagesDto dto, Conversation conversation) {
		String title = conversation.getTitle();
		List<Message> messages = conversation.getMessages();
		List<MessagesVo> messagesVos = messages.stream()
			  .skip((long) dto.offset() * dto.limit())
			  .limit(dto.limit())
			  .map(message -> new MessagesVo(message.getContent(), message.getRole().toString()))
			  .toList();
		Pagination pagination = new Pagination((long) messages.size(), messages.size() / dto.limit() + 1, dto.offset(), dto.limit());
		return new GetConversationsMessagesVo(title, messagesVos, pagination);
	}

	private void onError(SseEmitter emitter, String errorMessage, Exception e) {
		sendErrorEvent(emitter, errorMessage);
		emitter.completeWithError(e);
	}

	private void sendErrorEvent(SseEmitter emitter, String errorMessage) {
		try {
			emitter.send(SseEmitter.event()
				  .name("error")
				  .data(errorMessage));
		} catch (IOException e) {
			log.error("Error sending error event: {}", e.getMessage());
			emitter.completeWithError(e);
		}
	}

	private void sendDoneEvent(SseEmitter emitter, Long conversationId, String title) throws IOException {
		Map<String, String> doneData = Map.of(
			  "conversationId", String.valueOf(conversationId),
			  "title", title
		);
		String doneDataString = objectMapper.writeValueAsString(doneData);
		emitter.send(SseEmitter.event()
			  .name("done")
			  .data(doneDataString));
	}

	/**
	 * Stream AI response via SseEmitter and return CompletableFuture for full response
	 * @param userMessage
	 * @param emitter
	 * @return
	 */
	@NotNull
	private CompletableFuture<String> streamChat(String userMessage, SseEmitter emitter) {
		CompletableFuture<String> responseFuture = new CompletableFuture<>();
		StreamingChatResponseHandler streamingChatResponseHandler = new SseEmitterHandler(emitter, responseFuture);
		chatService.streamChat(userMessage, streamingChatResponseHandler);
		return responseFuture;
	}
}
