package com.pluto.pluto_interview.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record NewSettings(
	  @NotNull
	  List<String> preferredQuestionTypes,

	  @NotBlank
	  String preferredDifficultyLevel) {
}
