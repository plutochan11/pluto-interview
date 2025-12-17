package com.pluto.pluto_interview.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum DifficultyLevel {
	EASY("Easy"),
	MEDIUM("Medium"),
	HARD("Hard");

	private final String difficultyLevel;
}
