package com.pluto.pluto_interview.model.vo;

import lombok.NonNull;

import java.util.List;

public record SettingsDetail(
	  @NonNull
	  List<String> questionTypes,

	  @NonNull
	  List<String> preferredQuestionTypes,

	  @NonNull
	  List<String> difficultyLevels,

	  @NonNull
	  String preferredDifficultyLevel
) {
}
