package com.pluto.pluto_interview.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettingsResponse {
	private List<String> questionTypes;
	private List<String> preferredQuestionTypes;
	private List<String> difficultyLevel;
	private String preferredDifficultyLevel;
}
