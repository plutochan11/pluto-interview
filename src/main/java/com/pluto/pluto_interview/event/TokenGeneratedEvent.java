package com.pluto.pluto_interview.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class TokenGeneratedEvent extends ApplicationEvent {
	private final String token;

	public TokenGeneratedEvent(Object source, String token) {
		super(source);
		this.token = token;
	}
}
