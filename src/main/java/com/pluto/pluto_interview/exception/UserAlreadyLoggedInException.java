package com.pluto.pluto_interview.exception;

public class UserAlreadyLoggedInException extends RuntimeException {
	public UserAlreadyLoggedInException(String message) {
		super(message);
	}
}
