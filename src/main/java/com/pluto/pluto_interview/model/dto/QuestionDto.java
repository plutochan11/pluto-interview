package com.pluto.pluto_interview.model.dto;

import jakarta.validation.constraints.NotBlank;
import org.springframework.lang.Nullable;

public record QuestionDto(@NotBlank String title, @Nullable String content, @NotBlank String answer, @NotBlank String questionType, @NotBlank String difficultyLevel, @Nullable String from) {
}
