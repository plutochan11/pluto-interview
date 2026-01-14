package com.pluto.pluto_interview.exception;

public class EmailAlreadyRegisteredException extends RuntimeException {
	public EmailAlreadyRegisteredException(String message) {
		super(message);
	}
}
