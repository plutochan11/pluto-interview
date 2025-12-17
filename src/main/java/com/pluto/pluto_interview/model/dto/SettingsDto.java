package com.pluto.pluto_interview.model.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record SettingsDto(List<String> preferredQuestionTypes, @NotBlank  String preferredDifficultyLevel) {
}
