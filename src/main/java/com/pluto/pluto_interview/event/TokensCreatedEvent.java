package com.pluto.pluto_interview.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class TokensCreatedEvent extends ApplicationEvent {
	private final String token;
	private final long tokenTtlSeconds;
	private final String refreshToken;
	private final long refreshTokenTtlSeconds;
	private final Long userId;

	public TokensCreatedEvent(Object source, String token, long tokenTtlSeconds, String refreshToken, long refreshTokenTtlSeconds, Long userId) {
		super(source);
		this.token = token;
		this.tokenTtlSeconds = tokenTtlSeconds;
		this.refreshToken = refreshToken;
		this.refreshTokenTtlSeconds = refreshTokenTtlSeconds;
		this.userId = userId;
	}
}
