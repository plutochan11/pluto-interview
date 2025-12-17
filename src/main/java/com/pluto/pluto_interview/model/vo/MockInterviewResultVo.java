package com.pluto.pluto_interview.model.vo;

import jakarta.validation.constraints.NotBlank;
import org.jetbrains.annotations.NotNull;

public record MockInterviewResultVo(
	  @NotNull Double score,
//	  String duration,
//
//	  String averageResponseTime,

	  @NotNull
	  Integer questionsAnswered,

	  @NotBlank
	  String strengths,

	  @NotBlank
	  String improvements,

	  @NotBlank
	  String overallFeedback
) {
}
