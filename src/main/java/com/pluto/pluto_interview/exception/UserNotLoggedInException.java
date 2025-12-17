package com.pluto.pluto_interview.exception;

public class UserNotLoggedInException extends RuntimeException {
	public UserNotLoggedInException(String message) {
		super(message);
	}
}
