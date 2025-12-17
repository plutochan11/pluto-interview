package com.pluto.pluto_interview.model.vo;

import com.pluto.pluto_interview.model.dto.GetConversationsMessagesDto;
import com.pluto.pluto_interview.model.dto.Pagination;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record GetConversationsMessagesVo(@NotBlank String title, @NotEmpty List<MessagesVo> messages, @NotNull Pagination pagination) {
	public GetConversationsMessagesVo {
		messages = List.copyOf(messages);
	}
}
