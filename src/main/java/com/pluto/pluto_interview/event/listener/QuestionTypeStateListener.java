package com.pluto.pluto_interview.event.listener;

import com.pluto.pluto_interview.enums.ErrorMessage;
import com.pluto.pluto_interview.event.QuestionAddedEvent;
import com.pluto.pluto_interview.exception.IllegalQuestionTypeException;
import com.pluto.pluto_interview.model.Question;
import com.pluto.pluto_interview.model.QuestionType;
import com.pluto.pluto_interview.repository.QuestionTypeRepository;
import com.pluto.pluto_interview.service.QuestionTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

//@Component
@RequiredArgsConstructor
public class QuestionTypeStateListener{
	private final QuestionTypeService questionTypeService;
	private final QuestionTypeRepository questionTypeRepository;

	@Async("taskExecutor")
//	@TransactionalEventListener(classes = QuestionAddedEvent.class)
	public void onQuestionAddedEvent(QuestionAddedEvent questionAddedEvent) {
		// Find out what is the question type of the added question
		String questionTypeName = questionAddedEvent.getQuestion().getQuestionType().toString();
		QuestionType questionType = questionTypeRepository.findByName(questionTypeName)
			  .orElseThrow(() ->
				    new IllegalQuestionTypeException(ErrorMessage
					      .ILLEGAL_QUESTION_TYPE.getErrorMessage()));
		// Increment the question count of that question type
		questionType.incrementQuestionCount();

		// Persist the updated question type
		questionTypeRepository.save(questionType);
	}
}
