package com.pluto.pluto_interview.model.dto;

import jakarta.validation.constraints.NotNull;

public record GetMockInterviewSessionsDto(Integer offset, Integer limit) {
	public GetMockInterviewSessionsDto {
		offset = offset == null ? 0 : offset;
		limit = limit == null ? 10 : limit;
	}
}
