package com.pluto.pluto_interview.model.vo;

import jakarta.validation.constraints.NotBlank;

public record GetQuestionByIdVo(
	  @NotBlank
	  String difficultyLevel,

	  @NotBlank
	  String questionType,

	  @NotBlank
	  String title,

	  @NotBlank
	  String content,

	  @NotBlank
	  String answer
) {
}
