package com.pluto.pluto_interview.event;

import com.pluto.pluto_interview.model.Message;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.ArrayList;
import java.util.List;

@Getter
public class UserMessageResponsedEvent extends ApplicationEvent {
	private final List<Message> messages = new ArrayList<>();


	public UserMessageResponsedEvent(Object source, List<Message> messages) {
		super(source);
		this.messages.addAll(messages);
	}
}
