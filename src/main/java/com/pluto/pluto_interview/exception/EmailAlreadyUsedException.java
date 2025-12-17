package com.pluto.pluto_interview.exception;

public class EmailAlreadyUsedException extends RuntimeException {
	public EmailAlreadyUsedException(String message) {
		super(message);
	}
}
