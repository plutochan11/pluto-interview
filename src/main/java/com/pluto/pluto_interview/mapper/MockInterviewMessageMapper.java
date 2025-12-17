package com.pluto.pluto_interview.mapper;

import com.pluto.pluto_interview.model.MockInterviewMessage;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MockInterviewMessageMapper {
	default ChatMessage toChatMessage(MockInterviewMessage source) {
		if (source == null) {
			return null;
		}

		String role = source.getRoleAsString();
		String content = source.getContent();

		return switch (role.toLowerCase()) {
			case "interviewer" -> new AiMessage(content);
			case "candidate" -> new UserMessage(content);
			default -> new UserMessage(content);
		};
	}
}
