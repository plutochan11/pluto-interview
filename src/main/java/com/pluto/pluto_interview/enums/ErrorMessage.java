package com.pluto.pluto_interview.enums;

import lombok.Getter;

@Getter
public enum ErrorMessage {
	EMAIL_ALREADY_REGISTERED("Email Already Registered"),
	EMAIL_NOT_REGISTERED("Email address not registered."),
	WRONG_PASSWORD("Wrong password."),
	NOT_LOGGED_IN("You are not logged in. (or your session has expired)"),
	USER_NOT_FOUND("User not found."),
	ILLEGAL_QUESTION_TYPE("Illegal question type provided."),
	ILLEGAL_DIFFICULTY_LEVEL("Illegal difficulty level provided."),
	AI_RESPONSE_ERROR("AI response error occurred."),
	UNEXPECTED_ERROR_HAPPENED("Some unexpected errors happened, please try later."),
	CONVERSATION_NOT_FOUND("Conversation not found."),
	INVALID_CONVERSATION_FORMAT("Invalid conversation ID format."),
	STREAMING_RESPONSE_TIMEOUT("Request timeout, please try again."),
	NOT_YOUR_CONVERSATION("You are accessing a conversation that doesn't belong to you."),
	LOGIN_EXPIRED("Session expired, you need to log in again."),
	MOCK_INTERVIEW_SESSION_NOT_FOUND("Mock interview session not found."),
	MOCK_INTERVIEW_SESSION_OWNERSHIP_VIOLATION("Unauthorised"),
	QUESTION_NOT_FOUND("Question not found"),
	SERVICE_TIMEOUT("Service timeout, please try again later."),
	RATE_LIMIT_EXCEEDED("Rate limit exceeded. Please try again later."),
	REFRESH_TOKEN("Token Expired, Refresh Token First"),
	TOKEN_EXPIRED("The token has expired."),
	UNKNOWN_TOKEN("Unknown token."),
	ILLEGAL_TOKEN("Your user info cannot be recognised. Please log in again."),
	EMAIL_REGISTERED("Email is registered, please log in.");

	private final String errorMessage;

	ErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
}
