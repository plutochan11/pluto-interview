package com.pluto.pluto_interview.service;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RedisService implements CacheService{
	private final StringRedisTemplate stringRedisTemplate;

	@Override
	public void save(String key, String value) {
		stringRedisTemplate.opsForValue()
			  .set(key, value);
	}

	@Override
	public void save(String key, String value, Duration duration) {
		stringRedisTemplate.opsForValue()
			  .set(key,value, duration);
	}

	@Override
	public String getString(String key) {
		return stringRedisTemplate.opsForValue()
			  .get(key);
	}

	@Override
	public Map<Object, Object> getHash(String key) {
		return stringRedisTemplate.opsForHash()
			  .entries(key);
	}

	@Override
	public void delete(String key) {
		stringRedisTemplate.delete(key);
	}

	@Override
	public boolean containsKey(@NotNull String token) {
		return stringRedisTemplate.hasKey(token);
	}
}
