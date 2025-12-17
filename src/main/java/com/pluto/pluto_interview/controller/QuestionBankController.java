package com.pluto.pluto_interview.controller;

import com.pluto.pluto_interview.model.Question;
import com.pluto.pluto_interview.model.Response;
import com.pluto.pluto_interview.model.dto.QuestionBankDto;
import com.pluto.pluto_interview.model.dto.QuestionDto;
import com.pluto.pluto_interview.service.QuestionBankService;
import com.pluto.pluto_interview.util.UserIdUtil;
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

//	@GetMapping("/questions")
//	public ResponseEntity<Response> getQuestions(@RequestBody QuestionBankDto questionBankDto) {
//		Response response = questionBankService.getQuestions(questionBankDto);
//		return ResponseEntity.ok(response);
//	}

	@GetMapping("/questions")
	public ResponseEntity<Response> getQuestions(
		  @RequestParam(name = "offset", required = false, defaultValue = "0") Integer offset,
		  @RequestParam(name = "limit", required = false, defaultValue = "10") Integer limit,
		  @RequestParam(name = "query", required = false) String query,
		  @RequestParam(name = "questionType", required = false) String questionType,
		  @RequestParam(name = "difficultyLevel", required = false) String difficultyLevel)
		  throws ExecutionException, InterruptedException, TimeoutException {
		CompletableFuture<Response> responseCompletableFuture = questionBankService
			  .getQuestions(offset, limit, query, questionType, difficultyLevel);
		Response response = responseCompletableFuture.get(SERVICE_TIMEOUT, TimeUnit.SECONDS);
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
	public ResponseEntity<Response> getQuestionById(@PathVariable Long questionId) throws ExecutionException, InterruptedException, TimeoutException {
		CompletableFuture<Response> responseCompletableFuture = questionBankService.getQuestionById(questionId);
		Response response = responseCompletableFuture.get(SERVICE_TIMEOUT, TimeUnit.SECONDS);
		return ResponseEntity.ok(response);
	}
}
