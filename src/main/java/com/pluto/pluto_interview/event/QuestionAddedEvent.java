package com.pluto.pluto_interview.event;

import com.pluto.pluto_interview.model.Question;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class QuestionAddedEvent extends ApplicationEvent {
	private final Question question;

	public QuestionAddedEvent(Object source, Question question) {
		super(source);
		this.question = question;
	}
}
