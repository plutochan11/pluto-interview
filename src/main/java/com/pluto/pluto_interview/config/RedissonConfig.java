package com.pluto.pluto_interview.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {
	private static final String REDIS_ADDRESS = "redis://127.0.0.1:6379";

	@Bean
	public RedissonClient redissonClient() {
		Config config = new Config();
		config.useSingleServer()
			  .setAddress(REDIS_ADDRESS);

		return Redisson.create(config);
	}
}
