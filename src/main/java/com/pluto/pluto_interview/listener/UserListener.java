package com.pluto.pluto_interview.listener;

import com.pluto.pluto_interview.mapper.UserMapper;
import com.pluto.pluto_interview.model.User;
import com.pluto.pluto_interview.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;

// TODO Implement this listener
@Component
public class UserListener {
	private final String CACHE_KEY_PREFIX = "user:";
	private final StringRedisTemplate stringRedisTemplate;
	private final UserMapper userMapper;
	private final Duration CACHE_TTL;

	public UserListener(StringRedisTemplate stringRedisTemplate, UserMapper userMapper,
	                    @Value("${jwt.refreshTokenTtl}") Duration cacheTtl) {
		this.stringRedisTemplate = stringRedisTemplate;
		this.userMapper = userMapper;

		if (cacheTtl == null) {
			cacheTtl = Duration.ofDays(7);
		}
		CACHE_TTL = cacheTtl;
	}

	@EventListener(AuthenticationService.UserCreatedEvent.class)
	public void cacheUser(AuthenticationService.UserCreatedEvent event) {
		// Get the user from the event
		User user = event.getUser();
		
		// Generate a cache key with the user ID
		String key = CACHE_KEY_PREFIX + user.getId();

		// Cache the user info
		Map<String, String> map = userMapper.toMap(user);
		stringRedisTemplate.opsForHash().putAll(key, map);
		stringRedisTemplate.expire(key, CACHE_TTL);
	}
}
