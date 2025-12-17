package com.pluto.pluto_interview.enums;

import lombok.Getter;

@Getter
public enum TokenStatus {
	VALID("1"),
	INVALID("0");

	private final String code;

	TokenStatus(String code) {
		this.code = code;
	}
}
