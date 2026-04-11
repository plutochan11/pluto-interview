package com.pluto.pluto_interview.config;

import com.pluto.pluto_interview.config.properties.ThreadPoolProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.task.DelegatingSecurityContextTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@RequiredArgsConstructor
public class ExecutorConfig {
	private final ThreadPoolProperties threadPoolProperties;

	@Bean
	@Primary
	public Executor executor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(threadPoolProperties.getCorePoolSize());
		executor.setMaxPoolSize(threadPoolProperties.getMaxPoolSize());
		executor.setQueueCapacity(threadPoolProperties.getQueueCapacity());
		executor.setKeepAliveSeconds(threadPoolProperties.getKeepAliveSeconds());
		executor.setThreadNamePrefix("executor-");
		executor.initialize();

		return new DelegatingSecurityContextTaskExecutor(executor);
	}
}
