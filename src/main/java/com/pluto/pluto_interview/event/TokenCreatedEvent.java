package com.pluto.pluto_interview.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class TokenCreatedEvent extends ApplicationEvent {
	private final String token;
	private final String tokenCacheKey;

	public TokenCreatedEvent(Object source, String token, String tokenCacheKey) {
		super(source);
		this.token = token;
		this.tokenCacheKey = tokenCacheKey;
	}
}
