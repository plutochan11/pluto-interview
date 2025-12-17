package com.pluto.pluto_interview.config;

import com.pluto.pluto_interview.config.properties.ThreadPoolProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

//@Configuration
@RequiredArgsConstructor
public class ThreadPoolConfig {
	private final ThreadPoolProperties threadPoolProperties;

	@Bean
	public Executor executor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(threadPoolProperties.getCorePoolSize());
		executor.setMaxPoolSize(threadPoolProperties.getMaxPoolSize());
		executor.setQueueCapacity(threadPoolProperties.getQueueCapacity());
		executor.setKeepAliveSeconds(threadPoolProperties.getKeepAliveSeconds());
		executor.setThreadNamePrefix("task-executor-");
		executor.initialize();
		return executor;
	}
}
