package com.pluto.pluto_interview.exception;

public class TokenAlreadyExpiredException extends RuntimeException {
	public TokenAlreadyExpiredException(String message) {
		super(message);
	}
}
