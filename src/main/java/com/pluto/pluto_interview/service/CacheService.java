package com.pluto.pluto_interview.service;

import jakarta.validation.constraints.NotNull;

import java.time.Duration;
import java.util.Map;

public interface CacheService {
	void save(String key, String value);
	void save(String key, String value, Duration duration);

	/**
	 * Get String value by key.
	 * @return The value associated with the key, or null if not found
	 */
	String getString(String key);
	Map<Object, Object> getHash(String key);
	void delete(String key);

	boolean containsKey(@NotNull String key);
}
