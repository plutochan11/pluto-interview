package com.pluto.pluto_interview.model.dto;

import org.springframework.lang.Nullable;

public record GetConversationsDto(@Nullable Integer offset, @Nullable Integer limit, @Nullable String qeury) {
	public GetConversationsDto {
		offset = offset == null ? 0 : offset;
		limit = limit == null ? 10 : limit;
	}
}
