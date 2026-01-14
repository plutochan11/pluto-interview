package com.pluto.pluto_interview.event.listener;

import com.pluto.pluto_interview.enums.ErrorMessage;
import com.pluto.pluto_interview.event.TokenCreatedEvent;
import com.pluto.pluto_interview.exception.ExpiredTokenException;
import com.pluto.pluto_interview.exception.UnknownTokenException;
import com.pluto.pluto_interview.service.CacheService;
import com.pluto.pluto_interview.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtRedisStorageListener {
	@Autowired
	private JwtService tokenService;
	@Autowired
	private CacheService cacheService;

	/**
	 * Save the created token to the cache.
	 * @param event
	 */
	@Async
	@EventListener(TokenCreatedEvent.class)
	public void save(TokenCreatedEvent event) {
		// Extract the token and the cache key
		String token = event.getToken();
		String key = event.getTokenCacheKey();

		// Extract expiry while checking the life state and validity
		try {
			Date expiry = tokenService.getExpiry(token);
			// Save it to the store
			cacheService.save(key, token, Duration.between(Instant.now(), expiry.toInstant()));
		} catch (ExpiredJwtException e) {
			throw new ExpiredTokenException(ErrorMessage.TOKEN_EXPIRED.getErrorMessage());
		} catch (JwtException e) {
			throw new UnknownTokenException(ErrorMessage.UNKNOWN_TOKEN.getErrorMessage());
		}
	}
}
