package com.pluto.pluto_interview.exception;

import lombok.Getter;

@Getter
public class WrongPasswordException extends RuntimeException {
	private final String emailToLogIn;

	public WrongPasswordException(String message, String emailToLogIn) {
		super(message);
		this.emailToLogIn = emailToLogIn;
	}
}
