package com.pluto.pluto_interview.config.properties;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.lang.Nullable;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@ConfigurationProperties("jwt")
@Data
public class JwtProperties {
	private String secret;
	private long ttl = 1;
	private TimeUnit timeUnit = TimeUnit.HOURS;
	private Duration refreshTokenTtl = Duration.ofDays(7); // 7 days
	private String issuer = "";
}
