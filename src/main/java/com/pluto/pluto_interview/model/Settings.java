package com.pluto.pluto_interview.model;

import com.pluto.pluto_interview.mapper.QuestionTypeMapper;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.lang.Nullable;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Settings {
	@Id
	private Long userId;

	@Column(nullable = false)
	private String preferredQuestionTypes;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Question.DifficultyLevel preferredDifficultyLevel;

	@OneToOne(fetch = FetchType.LAZY)
	@MapsId
	@JoinColumn(nullable = false)
	@Setter(AccessLevel.NONE)
	private User user;

	public static Settings createDefault(User user) {
		List<Question.QuestionType> questionTypes = getDefaultQuestionTypes();
		String questionTypeStr = QuestionTypeMapper.toString(questionTypes);

		return Settings.builder()
			  .userId(user.getId())
			  .preferredQuestionTypes(questionTypeStr)
			  .preferredDifficultyLevel(getDefaultDifficultyLevel())
			  .user(user)
			  .build();
	}

	public void adjustPreferredQuestionTypes(@Nullable List<Question.QuestionType> preferredQuestionTypes) {
		if (preferredQuestionTypes == null) {
			this.preferredQuestionTypes = null;
			return;
		}

		this.preferredQuestionTypes = QuestionTypeMapper.toString(preferredQuestionTypes);
	}

	private static List<Question.QuestionType> getDefaultQuestionTypes() {
		return List.of(Question.QuestionType.BEHAVIOURAL, Question.QuestionType.CODING);
	}

	private static Question.@org.jspecify.annotations.NonNull DifficultyLevel getDefaultDifficultyLevel() {
		return Question.DifficultyLevel.MEDIUM;
	}
}
