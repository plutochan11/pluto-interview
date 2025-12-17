package com.pluto.pluto_interview.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionResult {
	private Long id;
	private String title;
	private String questionType;
	private String difficultyLevel;
	@Nullable
	private String from;
}
