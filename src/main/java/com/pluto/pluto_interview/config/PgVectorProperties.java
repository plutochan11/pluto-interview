package com.pluto.pluto_interview.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pgvector")
@Data
public class PgVectorProperties {
	private String host;
	private int port;
	private String database;
	private String user;
	private String password;
	private String table;
	private int dimesion = 1536;
}
