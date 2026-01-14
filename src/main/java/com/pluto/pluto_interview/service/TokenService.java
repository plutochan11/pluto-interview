package com.pluto.pluto_interview.service;

import org.springframework.lang.Nullable;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public interface TokenService {
	/**
	 * Generate a token with the given claims and time to live.
	 * @param claims Claims containing the payload
	 * @param ttl Optional
	 * @param timeUnit Optional
	 * @return
	 */
	String generate(Map<String, Object> claims, @Nullable Long ttl, @Nullable TimeUnit timeUnit);

	/**
	 * Generate a refresh token with the given claims.
	 * @param claims
	 * @return A refresh token
	 */
	String generateRefreshToken(Map<String, Object> claims);

	Map<String, Object> parse(String token);

	/**
	 * Check if the token has expired
	 * @param token
	 * @return True if expired, false otherwise
	 */
	boolean hasExpired(String token);

	Date getExpiry(String token);
}
