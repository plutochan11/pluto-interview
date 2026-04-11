package com.pluto.pluto_interview.mapper;

import com.pluto.pluto_interview.model.Question;
import org.mapstruct.Mapper;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class QuestionTypeMapper {
	public static final String SEPARATOR = ", ";

	public static String toString(List<Question.QuestionType> questionTypes) {
		if (questionTypes == null || questionTypes.isEmpty()) {
			return "";
		}
		return questionTypes.stream()
			  .map(Question.QuestionType::name)
			  .collect(Collectors.joining(SEPARATOR));
	}

	public static List<Question.QuestionType> toList(String questionTypes) {
		if (questionTypes == null || questionTypes.isBlank()) {
			return List.of();
		}
		return Arrays.stream(questionTypes.split(SEPARATOR))
			  .map(Question.QuestionType::valueOf)
			  .toList();
	}

	public static List<String> toStringList(String questionTypes) {
		if (questionTypes == null || questionTypes.isBlank()) {
			return List.of();
		}
		return Arrays.stream(questionTypes.split(SEPARATOR))
			  .toList();
	}
}
