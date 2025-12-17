package com.pluto.pluto_interview.mapper;

import com.pluto.pluto_interview.model.MockInterviewResult;
import com.pluto.pluto_interview.model.vo.MockInterviewResultVo;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MockInterviewResultMapper {
	default MockInterviewResultVo toVo(MockInterviewResult result) {
		if (result == null) {
			return null;
		}

		return new MockInterviewResultVo(
			  result.getScore(),
//			  null,
//			  null,
			  result.getQuestionAnswered(),
			  result.getStrengths(),
			  result.getImprovements(),
			  result.getOverallFeedback()
		);
	}
}
