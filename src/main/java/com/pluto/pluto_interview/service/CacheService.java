package com.pluto.pluto_interview.service;

import jakarta.validation.constraints.NotNull;

import java.time.Duration;

public interface CacheService {
	void save(String key, String value);
	void save(String key, String value, Duration duration);

	/**
	 * Get value by key.
	 * @param key
	 * @return The value associated with the key, or null if not found
	 */
	String get(String key);
	void delete(String key);

	boolean containsKey(@NotNull String key);
}
