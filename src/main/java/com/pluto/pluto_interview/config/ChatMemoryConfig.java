package com.pluto.pluto_interview.config;

import com.pluto.pluto_interview.store.PostgresChatMemoryStore;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatMemoryConfig {
	@Bean
	public ChatMemoryProvider chatMemoryProvider(ChatMemoryStore chatMemoryStore) {
		return memoryId -> MessageWindowChatMemory.builder()
			  .id(memoryId)
			  .maxMessages(20)
			  .chatMemoryStore(chatMemoryStore)
			  .build();
	}
}
