package com.pluto.pluto_interview.model.vo;

import com.pluto.pluto_interview.model.dto.Pagination;
import org.springframework.lang.Nullable;

import java.util.List;

public record GetConversationsVo(@Nullable List<ConversationVo> conversations, @Nullable Pagination pagination) {
	public GetConversationsVo {
		conversations = conversations == null ? null : List.copyOf(conversations);
	}
}
