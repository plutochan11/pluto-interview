package com.pluto.pluto_interview.exception;

import com.pluto.pluto_interview.enums.ErrorMessage;
import com.pluto.pluto_interview.model.Response;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class GlobalExceptionHandler {
	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<Response> handleIllegalStateException(IllegalStateException e,
	                                                            HttpServletRequest request) {
		Response errorResponse = Response.error(e.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	@ExceptionHandler(NoSuchElementException.class)
	public ResponseEntity<Response> handleNoSuchElementException(NoSuchElementException e,
	                                                             HttpServletRequest request) {
		Response errorResponse = Response.error(e.getMessage());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Response> handleIllegalArgumentException(
		  IllegalArgumentException ex, HttpServletRequest request) {
		String message = ex.getMessage();
		String errorMessage = "";
		String userInfo = "";
		for (String s : message.split(";")) {
			if (s.startsWith("Invalid")) {
				errorMessage = s.trim();
			} else if (s.startsWith("User(")) {
				userInfo = s.trim();
			}
		}

		Response response = Response.error(errorMessage);
		log.info("{} passed illegal argument(s) at {}", userInfo, request.getRequestURI());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	@ExceptionHandler(QuestionNotFoundException.class)
	public ResponseEntity<Response> handleQuestionNotFoundException(
		  QuestionNotFoundException ex, HttpServletRequest request) {
		Response response = Response.error(ex.getMessage());
		log.info("A question not found exception occurred at {}",
			  request.getRequestURI());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	}

	@ExceptionHandler(ExpiredJwtException.class)
	public ResponseEntity<Response> handleExpiredJwtException(
		  ExpiredJwtException ex, HttpServletRequest request) {
		Response response = Response.error(ErrorMessage.LOGIN_EXPIRED.getErrorMessage());
		log.info("An expired JWT token was used at {}",
			  request.getRequestURI());
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
	}

	@ExceptionHandler(exception = ExecutionException.class)
	public ResponseEntity<Response> handleExecutionException(
		  Exception ex, HttpServletRequest request) throws Throwable {
		Throwable cause = ex.getCause();
		Response response;

		if (cause == null) {
			response = Response.error(ErrorMessage.UNEXPECTED_ERROR_HAPPENED.getErrorMessage());
		} else {
			throw cause;
		}
		log.error("Asynchronous error occurred at {}: {}", request.getRequestURI(), ex.getMessage());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}

	@ExceptionHandler(exception = TimeoutException.class)
	public ResponseEntity<Response> handleTimeoutException(
		  Exception ex, HttpServletRequest request) {
		Response response = Response.error(ErrorMessage.SERVICE_TIMEOUT.getErrorMessage());
		log.error("Service timeout occurred at {}: {}", request.getRequestURI(), ex.getMessage());
		return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(response);
	}

	@ExceptionHandler(InterruptedException.class)
	public ResponseEntity<Response> handleInterruptedException(
		  InterruptedException ex, HttpServletRequest request) {
		Response response = Response.error(ErrorMessage.UNEXPECTED_ERROR_HAPPENED.getErrorMessage());
		log.error("Service interrupted at {}: {}", request.getRequestURI(), ex.getMessage());
		Thread.currentThread().interrupt(); // Restore the interrupted status
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}

	@ExceptionHandler(UserNotLoggedInException.class)
	public ResponseEntity<Response> handleUserNotLoggedInException(
		  UserNotLoggedInException ex, HttpServletRequest request) {
		Response response = Response.error(ex.getMessage());
		log.info("A user not logged in exception occurred at {}",
			  request.getRequestURI());
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
	}

//	@ExceptionHandler(RuntimeException.class)
//	public ResponseEntity<Response> handleRuntimeException(
//		  RuntimeException ex, HttpServletRequest request) {
//		Response response = Response.error("An unexpected error occurred, please try again later");
//		log.error("Unexpected error occurred at {}: {}", request.getRequestURI(), ex.getMessage());
//		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//	}

	@ExceptionHandler(AiResponseErrorException.class)
	public ResponseEntity<Response> handleAiResponseErrorException(
		  AiResponseErrorException ex, HttpServletRequest request) {
		Response response = Response.error(ex.getMessage());
		log.error("AI response error occurred at {}: {}", request.getRequestURI(), ex.getMessage());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}

	@ExceptionHandler(EmailAlreadyUsedException.class)
	public ResponseEntity<Response> handleEmailAlreadyUsedException(
		  EmailAlreadyUsedException ex, HttpServletRequest request) {
		Response response = Response.error(ex.getMessage());
		log.info("A new user attempted to register with an used email at {}",
			  request.getRequestURI());
		return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
	}

	@ExceptionHandler(EmailNotRegisteredException.class)
	public ResponseEntity<Response> handleEmailNotRegisteredException(
		  EmailNotRegisteredException ex, HttpServletRequest request) {
		Response response = Response.error(ex.getMessage());
		log.info("A login attempt with unregistered email at {}",
			  request.getRequestURI());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	}

	@ExceptionHandler(WrongPasswordException.class)
	public ResponseEntity<Response> handleWrongPasswordException(
		  WrongPasswordException ex, HttpServletRequest request) {
		Response response = Response.error(ex.getMessage());
		log.info("A login attempt to email({}) with wrong password at {}",
			  ex.getEmailToLogIn(), request.getRequestURI());
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
	}

	@ExceptionHandler(IllegalQuestionTypeException.class)
	public ResponseEntity<Response> handleIllegalQuestionTypeException(
		  IllegalQuestionTypeException ex, HttpServletRequest request) {
		Response response = Response.error(ex.getMessage());
		log.info("An illegal interview type was provided at {}",
			  request.getRequestURI());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	@ExceptionHandler(IllegalDifficultyLevelException.class)
	public ResponseEntity<Response> handleIllegalDifficultyLevelException(
		  IllegalDifficultyLevelException ex, HttpServletRequest request) {
		Response response = Response.error(ex.getMessage());
		log.info("An illegal difficulty level was provided at {}",
			  request.getRequestURI());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<Response> handleUserNotFoundException(
		  UserNotFoundException ex, HttpServletRequest request) {
		Response response = Response.error(ex.getMessage());
		log.info("A user not found exception occurred (URI: {}), (Cause: {})",
			  request.getRequestURI(), ex.getCause().getMessage());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	}
}
