package com.pluto.pluto_interview.model.vo;

import com.pluto.pluto_interview.model.dto.Pagination;
import com.pluto.pluto_interview.model.dto.QuestionResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuestionsVo {
	private List<QuestionResult> questions;
	private Pagination pagination;
}
