package com.pluto.pluto_interview.exception;

public class ExpiredTokenException extends RuntimeException {
	public ExpiredTokenException(String message) {
		super(message);
	}
}
