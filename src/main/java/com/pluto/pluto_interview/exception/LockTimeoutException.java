package com.pluto.pluto_interview.exception;

public class LockTimeoutException extends RuntimeException {
	public LockTimeoutException(String message) {
		super(message);
	}
}
