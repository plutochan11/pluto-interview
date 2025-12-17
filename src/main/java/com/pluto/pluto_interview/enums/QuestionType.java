package com.pluto.pluto_interview.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum QuestionType {
	BEHAVIOURAL("Behavioural"),
	TECHNICAL("Technical"),
	CODING("Coding"),
	SYSTEM_DESIGN("System Design"),
	LEADERSHIP("Leadership"),
	PRODUCT("Product");

	private final String questionType;
}
