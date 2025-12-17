package com.pluto.pluto_interview.event.listener;

import com.pluto.pluto_interview.event.TokenGeneratedEvent;
import com.pluto.pluto_interview.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenStorageListener {
	private final TokenService tokenService;

	@Async("taskExecutor")
	@TransactionalEventListener(classes = TokenGeneratedEvent.class)
	public void onTokenGeneratedEvent(TokenGeneratedEvent tokenGeneratedEvent) {
		try {
			tokenService.save(tokenGeneratedEvent.getToken());
		} catch (Exception e) {
			log.warn("Failed to persist token to storage for event: {}, error: {}",
				  tokenGeneratedEvent, e.getMessage(), e);
		}
	}
}
