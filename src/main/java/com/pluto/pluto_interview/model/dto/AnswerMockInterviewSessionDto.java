package com.pluto.pluto_interview.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AnswerMockInterviewSessionDto(@NotNull Long sessionId, @NotBlank String answer) {
}
