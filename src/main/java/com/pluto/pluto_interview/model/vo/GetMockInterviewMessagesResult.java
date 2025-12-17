package com.pluto.pluto_interview.model.vo;

import com.pluto.pluto_interview.model.dto.Pagination;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record GetMockInterviewMessagesResult(
	  @NotEmpty List<MockInterviewMessageVo> messageVos,
	  @NotNull Pagination pagination
	  ) {
}
