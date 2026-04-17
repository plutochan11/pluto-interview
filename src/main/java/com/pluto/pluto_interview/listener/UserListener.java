package com.pluto.pluto_interview.listener;

import com.pluto.pluto_interview.mapper.UserMapper;
import com.pluto.pluto_interview.event.UserCreatedEvent;
import com.pluto.pluto_interview.event.UserEvent;
import com.pluto.pluto_interview.event.UserLoggedInEvent;
import com.pluto.pluto_interview.event.UserLoggedOutEvent;
import com.pluto.pluto_interview.model.Settings;
import com.pluto.pluto_interview.model.User;
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
	@EventListener(classes = {UserCreatedEvent.class, UserLoggedInEvent.class})
	public void cacheUser(UserEvent event) {
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
	@EventListener(UserLoggedOutEvent.class)
	public void evictLoggedOutUserCache(UserLoggedOutEvent event) {
		Long userId = event.getUserId();
		evictUserInfo(userId);
	}

	@Async
	@EventListener(UserService.UserDeletedEvent.class)
	public void evictDeletedUserCache(UserService.UserDeletedEvent event) {
		Long userId = event.userId();
		evictUserInfo(userId);
	}

	/**
	 * Evict user information stored in cache.
	 * @param userId Used to compose of the key of user info.
	 */
	private void evictUserInfo(Long userId) {
		String key = CACHE_KEY_PREFIX + userId;
		stringRedisTemplate.delete(key);
	}
}
