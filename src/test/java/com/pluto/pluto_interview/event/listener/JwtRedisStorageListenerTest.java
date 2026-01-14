package com.pluto.pluto_interview.event.listener;

import com.pluto.pluto_interview.event.TokenCreatedEvent;
import com.pluto.pluto_interview.exception.ExpiredTokenException;
import com.pluto.pluto_interview.exception.UnknownTokenException;
import com.pluto.pluto_interview.service.CacheService;
import com.pluto.pluto_interview.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtRedisStorageListenerTest {
	@Mock
	private JwtService tokenService;
	@Mock
	private CacheService cacheService;
	@InjectMocks
	private JwtRedisStorageListener jwtRedisStorageListener;

	@Test
	void save_ShouldThrowExpiredTokenException_WhenTokenIsExpired() {
		String token = "expired token";
		String key = "key";
		TokenCreatedEvent event = new TokenCreatedEvent(this, token, key);

		when(tokenService.getExpiry(token)).thenThrow(ExpiredJwtException.class);

		assertThrows(ExpiredTokenException.class, () -> jwtRedisStorageListener.save(event));
	}

	@Test
	void save_ShouldThrowUnknownTokenException_WhenTokenIsInvalid() {
		String token = "invalid token";
		String key = "key";
		TokenCreatedEvent event = new TokenCreatedEvent(this, token, key);

		when(tokenService.getExpiry(token)).thenThrow(JwtException.class);

		assertThrows(UnknownTokenException.class, () -> jwtRedisStorageListener.save(event));
	}

	@Test
	void save_ShouldSaveTokenToCache() {
		String token = "valid token";
		String key = "key";
		TokenCreatedEvent event = new TokenCreatedEvent(this, token, key);

		when(tokenService.getExpiry(token)).thenReturn(Date.from(Instant.now().plusSeconds(60 * 60 * 24 * 7))); // 7 days from now

	}
}