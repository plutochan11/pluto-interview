package com.pluto.pluto_interview.mapper;

import com.pluto.pluto_interview.model.Question;
import com.pluto.pluto_interview.model.dto.QuestionDto;
import com.pluto.pluto_interview.model.vo.GetQuestionByIdVo;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface QuestionMapper {
//	@Mapping(source = "fromCompany", target = "from")
//	QuestionDto toDto(Question question);
//
//	@Mapping(source = "from", target = "fromCompany")
//	Question toEntity(QuestionDto questionDto);
//
//	List<QuestionDto> toDtoList(List<Question> questions);

	default GetQuestionByIdVo toGetQuestionByIdVo(Question question) {
		if (question == null) {
			return null;
		}
		return new GetQuestionByIdVo(
			  question.getDifficultyLevel().toString(),
			  question.getQuestionType().toString(),
			  question.getTitle(),
			  question.getContent(),
			  question.getAnswer()
		);
	}
}
