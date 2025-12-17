package com.pluto.pluto_interview.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record MockInterviewChatContext(@NotBlank String userMessage, @NotEmpty List<String> preferredQuestionTypes,
                                       @NotBlank String preferredDifficultyLevel, @NotEmpty List<Question> questionBank) {
}
