package com.pluto.pluto_interview.config.properties;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties("thread-pool")
@Getter
public class ThreadPoolProperties {
	private final int corePoolSize;
	private final int maxPoolSize;
	private final int queueCapacity;
	private final int keepAliveSeconds;

	@ConstructorBinding
	public ThreadPoolProperties(int corePoolSize, int maxPoolSize, int queueCapacity, int keepAliveSeconds) {
		this.corePoolSize = corePoolSize;
		this.maxPoolSize = maxPoolSize;
		this.queueCapacity = queueCapacity;
		this.keepAliveSeconds = keepAliveSeconds;
	}
}
