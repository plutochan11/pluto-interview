package com.pluto.pluto_interview.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Settings {
	@Id
	private Long userId;

	@Enumerated(EnumType.STRING)
	@ElementCollection
	@CollectionTable(name = "user_preferred_interview_types", joinColumns
		  = @JoinColumn(name = "user_id"))
	private List<Question.QuestionType> preferredQuestionTypes = new ArrayList<>();

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Question.DifficultyLevel preferredDifficultyLevel;

	@OneToOne
	@MapsId
	@JoinColumn(nullable = false)
	@Setter(AccessLevel.PRIVATE)
	private User user;

	public static Settings withDefault(User user) {
		List<Question.QuestionType> defaultQuestionTypes = List.of(Question.QuestionType.BEHAVIOURAL, Question.QuestionType.CODING);
		Question.DifficultyLevel defaultDifficultyLevel = Question.DifficultyLevel.MEDIUM;

		return Settings.builder()
			  .userId(user.getId())
			  .preferredQuestionTypes(defaultQuestionTypes)
			  .preferredDifficultyLevel(defaultDifficultyLevel)
			  .user(user)
			  .build();
	}

	/**
	 * Adjust preferred interview types.
	 * Pass null to clear the list.
	 * @param preferredInterviewTypes
	 */
	public void adjustPreferredQuestionTypes(@Nullable List<Question.QuestionType> preferredInterviewTypes) {
		if (preferredInterviewTypes == null) {
			this.preferredQuestionTypes.clear();
			return;
		}
		this.preferredQuestionTypes.clear();
		this.preferredQuestionTypes.addAll(preferredInterviewTypes);
	}
}
