package com.pluto.pluto_interview.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuestionBankDto {
	private Integer offset = 0;
	private Integer limit = 10;
	@Nullable
	private String query;
	@Nullable
	private String questionType;
	@Nullable
	private String difficultyLevel;
}
