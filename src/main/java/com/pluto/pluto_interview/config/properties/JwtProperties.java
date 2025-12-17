package com.pluto.pluto_interview.config.properties;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties("jwt")
@Getter
public class JwtProperties {
	private final String secret;
	private final long ttl;
	private final String issuer;

	@ConstructorBinding
	public JwtProperties(String secret, long ttl, String issuer) {
		this.secret = secret;
		this.ttl = ttl;
		this.issuer = issuer;
	}
}
