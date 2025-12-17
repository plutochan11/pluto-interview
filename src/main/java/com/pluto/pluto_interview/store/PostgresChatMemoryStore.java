package com.pluto.pluto_interview.store;

import com.pluto.pluto_interview.mapper.MockInterviewMessageMapper;
import com.pluto.pluto_interview.model.MockInterviewMessage;
import com.pluto.pluto_interview.repository.MockInterviewMessageRepository;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PostgresChatMemoryStore implements ChatMemoryStore {
	private final MockInterviewMessageRepository mockInterviewMessageRepo;
	private final MockInterviewMessageMapper mockInterviewMessageMapper;

	/**
	 * Get chat messages from the PostgreSQL database
	 * @param memoryId Must be ``Long``
	 * @return
	 */
	@Override
	public List<ChatMessage> getMessages(Object memoryId) {
		if (!(memoryId instanceof Long)) {
			throw new IllegalArgumentException("Memory ID must be of type Long");
		}

		List<MockInterviewMessage> messages = mockInterviewMessageRepo.findBySessionIdOrderByCreatedAtAsc(
			  (Long) memoryId
		);
		return messages.stream()
			  .map(mockInterviewMessageMapper::toChatMessage)
			  .toList();
	}

	@Override
	public void updateMessages(Object memoryId, List<ChatMessage> chatMessages) {

	}

	@Override
	public void deleteMessages(Object memoryId) {

	}
}
