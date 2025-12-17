package com.pluto.pluto_interview.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.lang.Nullable;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Question {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	@Column(nullable = false)
	private String title;

	@Nullable
	@Column(nullable = false)
	private String content;

	@NotBlank
	@Lob
	@Column(columnDefinition = "TEXT", nullable = false)
	private String answer;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private QuestionType questionType;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private DifficultyLevel difficultyLevel;

	@Nullable
	private String fromCompany;

	@CreationTimestamp
	@Column(updatable = false)
	private Instant createdAt;

	public enum QuestionType {
		BEHAVIOURAL,
		TECHNICAL,
		CODING,
		SYSTEM_DESIGN,
		LEADERSHIP,
		PRODUCT
	}

	public enum DifficultyLevel {
		EASY,
		MEDIUM,
		HARD
	}
}
