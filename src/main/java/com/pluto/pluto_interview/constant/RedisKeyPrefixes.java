package com.pluto.pluto_interview.constant;

public final class RedisKeyPrefixes {
	public static final String TOKEN_KEY_PREFIX = "token:";
	public static final String REFRESH_TOKEN_KEY_PREFIX = "refresh-token:";
	public static final String LOCK_NAME_PREFIX = "lock:auth-service:user-id:";

	private RedisKeyPrefixes() {
	}
}
