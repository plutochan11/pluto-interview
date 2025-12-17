package com.pluto.pluto_interview.model.vo;

import jakarta.validation.constraints.NotBlank;

public record MockInterviewMessageVo(
	  @NotBlank String content,
	  @NotBlank String role,
	  @NotBlank String createdAt) {
}
