package com.pluto.pluto_interview.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;
import org.springframework.lang.Nullable;

public record GetConversationsMessagesDto(@NotBlank String conversationId, @Nullable Integer offset, @Nullable Integer limit) {
	public GetConversationsMessagesDto {
		offset = offset == null ? 0 : offset;
		limit = limit == null ? 4 : limit;
	}
}
