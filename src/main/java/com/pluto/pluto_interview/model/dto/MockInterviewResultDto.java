package com.pluto.pluto_interview.model.dto;

public record MockInterviewResultDto(
	  Double score,
	  Integer questionAnswered,
	  String strengths,
	  String improvements,
	  String overallFeedback
) {
}
