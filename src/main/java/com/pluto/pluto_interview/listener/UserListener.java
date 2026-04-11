package com.pluto.pluto_interview.listener;

import com.pluto.pluto_interview.mapper.UserMapper;
import com.pluto.pluto_interview.model.Settings;
import com.pluto.pluto_interview.model.User;
import com.pluto.pluto_interview.service.AuthenticationService;
import com.pluto.pluto_interview.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
public class UserListener {
	public static final String CACHE_KEY_PREFIX = "user:";
	private final StringRedisTemplate stringRedisTemplate;
	private final UserMapper userMapper;
	private final Duration CACHE_TTL;

	private static final RedisScript<Void> SET_USER_SCRIPT = RedisScript.of(
		  new ClassPathResource("lua_scripts/set_user.lua"),
		  Void.class
	);

	public UserListener(StringRedisTemplate stringRedisTemplate, UserMapper userMapper,
	                    @Value("${jwt.refreshTokenTtl:7d}") Duration cacheTtl) {
		this.stringRedisTemplate = stringRedisTemplate;
		this.userMapper = userMapper;
		CACHE_TTL = cacheTtl;
	}

	/**
	 * Cache user on creation or login
	 * @throws IllegalArgumentException if user ID is null or user settings are null
	 */
	@EventListener(classes = {AuthenticationService.UserCreatedEvent.class, AuthenticationService.UserLoggedInEvent.class})
	public void cacheUser(AuthenticationService.UserEvent event) {
		User user = event.getUser();
		Long userId = user.getId();

		if (userId == null) {
			throw new IllegalArgumentException("User ID cannot be null.");
		}

		String key = CACHE_KEY_PREFIX + userId;

		Settings settings = user.getSettings();
		if (settings == null) {
			throw new IllegalArgumentException("User settings cannot be null.");
		}

		stringRedisTemplate.execute(
			  SET_USER_SCRIPT,
			  List.of(key),
			  "id", userId.toString(),
			  "email", user.getEmail(),
			  "username", user.getUsername(),
			  "preferredQuestionTypes", settings.getPreferredQuestionTypes(),
			  "preferredDifficultyLevel", settings.getPreferredDifficultyLevel().toString(),
			  String.valueOf(CACHE_TTL.toSeconds())
		);
	}

	@Async
	@EventListener(AuthenticationService.UserLoggedOutEvent.class)
	public void evictLoggedOutUserCache(AuthenticationService.UserLoggedOutEvent event) {
		Long userId = event.userid();
		evict(userId);
	}

	@Async
	@EventListener(UserService.UserDeletedEvent.class)
	public void evictDeletedUserCache(UserService.UserDeletedEvent event) {
		Long userId = event.userId();
		evict(userId);
	}

	private void evict(Long userId) {
		String key = CACHE_KEY_PREFIX + userId;
		stringRedisTemplate.delete(key);
	}
}
