package com.pluto.pluto_interview.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuestionType {
	@Id
	@SequenceGenerator(name = "question_type_id_sequence")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "question_type_id_sequence")
	private Long id;
	@NotBlank
	private String name;
	@NotBlank
	private String description;
	@NotNull
	private Integer questionCount;

	public void incrementQuestionCount() {
		this.questionCount = this.questionCount + 1;
	}
}
