package com.pluto.pluto_interview.exception;

public class UnknownTokenException extends RuntimeException {
	public UnknownTokenException(String message) {
		super(message);
	}

	public UnknownTokenException(Exception e) {
		super(e);
	}
}
