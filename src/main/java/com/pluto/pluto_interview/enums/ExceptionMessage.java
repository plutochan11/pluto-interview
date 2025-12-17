package com.pluto.pluto_interview.enums;

import lombok.Getter;

@Getter
public enum ExceptionMessage {
	TOKEN_ALREADY_EXPIRED("Token has already expired"),
	TOKEN_NOT_FOUND("Token not found in the store"),
	REQUEST_NOT_AUTHENTICATED("The request is not authenticated yet, the user ID is unavailable for now.");

	private final String message;

	ExceptionMessage(String message) {
		this.message = message;
	}
}
