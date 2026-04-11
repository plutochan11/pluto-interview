package com.pluto.pluto_interview.event.listener;

import com.pluto.pluto_interview.constant.TokenProperty;
import com.pluto.pluto_interview.event.TokensCreatedEvent;
import com.pluto.pluto_interview.service.AuthenticationService;
import com.pluto.pluto_interview.service.UserService;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TokenListener {
	private final StringRedisTemplate stringRedisTemplate;

	private static final RedisScript<Void> setTokenAndRefreshTokenScript =
		  RedisScript.of(
			    new ClassPathResource("lua_scripts/set_token_and_refresh_token.lua"),
			    Void.class
		  );
	private static final RedisScript<Void> deleteTokenAndRefreshTokenScript =
		  RedisScript.of(
			    new ClassPathResource("lua_scripts/delete_token_and_refresh_token.lua"),
			    Void.class
		  );

	public TokenListener(StringRedisTemplate stringRedisTemplate) {
		this.stringRedisTemplate = stringRedisTemplate;
	}

	@EventListener(TokensCreatedEvent.class)
	public void cacheToken(TokensCreatedEvent event) {
		Long userId = event.getUserId();

		// Cache token and refresh token
		String tokenKey = TokenProperty.TOKEN_KEY_PREFIX + userId;
		String refreshTokenKey = TokenProperty.REFRESH_TOKEN_KEY_PREFIX + userId;
		stringRedisTemplate.execute(setTokenAndRefreshTokenScript, List.of(tokenKey, refreshTokenKey),
			  event.getToken(), String.valueOf(event.getTokenTtlSeconds()), event.getRefreshToken(),
			  String.valueOf(event.getRefreshTokenTtlSeconds()));
	}

	@Async
	@EventListener(AuthenticationService.UserLoggedOutEvent.class)
	public void evictTokenCache(AuthenticationService.UserLoggedOutEvent event) {
		Long userId = event.userid();
		evict(userId);
	}

	@Async
	@EventListener(UserService.UserDeletedEvent.class)
	public void evictTokenCache(UserService.UserDeletedEvent event) {
		Long userId = event.userId();
		evict(userId);
	}

	private void evict(Long userId) {
		String tokenKey = TokenProperty.TOKEN_KEY_PREFIX + userId;
		String refreshTokenKey = TokenProperty.REFRESH_TOKEN_KEY_PREFIX + userId;

		stringRedisTemplate.execute(deleteTokenAndRefreshTokenScript, List.of(tokenKey, refreshTokenKey));
	}
}
