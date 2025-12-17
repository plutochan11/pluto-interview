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

	@ElementCollection
	@CollectionTable(name = "user_preferred_interview_types", joinColumns
		  = @JoinColumn(name = "user_id"))
	private List<String> preferredQuestionTypes = new ArrayList<>();
	private String preferredDifficultyLevel;

	@OneToOne
	@MapsId
	@JoinColumn
	@ToString.Exclude
	private User user;

	/**
	 * Adjust preferred interview types.
	 * Pass null to clear the list.
	 * @param preferredInterviewTypes
	 */
	public void adjustPreferredQuestionTypes(@Nullable List<String> preferredInterviewTypes) {
		if (preferredInterviewTypes == null) {
			this.preferredQuestionTypes.clear();
			return;
		}
		this.preferredQuestionTypes.clear();
		this.preferredQuestionTypes.addAll(preferredInterviewTypes);
	}
}
