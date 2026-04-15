package com.pluto.pluto_interview.controller;

import com.pluto.pluto_interview.model.Response;
import com.pluto.pluto_interview.model.dto.QuestionDto;
import com.pluto.pluto_interview.service.QuestionBankService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/question-bank")
public class QuestionBankController {

	private final QuestionBankService questionBankService;
	private final int SERVICE_TIMEOUT = 30; // seconds

	@GetMapping("/questions")
	public ResponseEntity<Response> getQuestions(
		  @RequestParam(name = "offset", required = false, defaultValue = "0") Integer offset,
		  @RequestParam(name = "limit", required = false, defaultValue = "10") Integer limit,
		  @RequestParam(name = "query", required = false) String query,
		  @RequestParam(name = "questionType", required = false) String questionType,
		  @RequestParam(name = "difficultyLevel", required = false) String difficultyLevel)
	{

		CompletableFuture<Response> responseCompletableFuture = questionBankService
			  .getQuestions(offset, limit, query, questionType, difficultyLevel);
		Response response = null;
		try {
			response = responseCompletableFuture.get(SERVICE_TIMEOUT, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return ResponseEntity.status(500).body(
				  Response.error("Service interrupted. Please try again later."));
		} catch (ExecutionException e) {
			// Rethrow with cause.
			throw new RuntimeException(e.getCause());
		} catch (TimeoutException e) {
			return ResponseEntity.status(504).body(
				  Response.error("Timeout. Please try again later."));
		}

		return ResponseEntity.ok(response);
	}

	@GetMapping("/question-types")
	public ResponseEntity<Response> getQuestionTypes() {
		Response response = questionBankService.getQuestionTypes();
		return ResponseEntity.ok(response);
	}

	@PostMapping("/questions")
	public ResponseEntity<Response> addQuestion(@Valid @RequestBody QuestionDto questionDto) {
		Response response = questionBankService.addQuestion(questionDto);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/questions/{questionId}")
	public ResponseEntity<Response> getQuestionById(@PathVariable Long questionId) {

		CompletableFuture<Response> responseCompletableFuture = questionBankService.getQuestionById(questionId);

		Response response = null;
		try {
			response = responseCompletableFuture.get(SERVICE_TIMEOUT, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return ResponseEntity.status(500).body(
				  Response.error("Service interrupted. Please try again later."));
		} catch (ExecutionException e) {
			// Rethrow with cause.
			throw new RuntimeException(e.getCause());
		} catch (TimeoutException e) {
			return ResponseEntity.status(504).body(
				  Response.error("Timeout. Please try again later."));
		}

		return ResponseEntity.ok(response);
	}
}
