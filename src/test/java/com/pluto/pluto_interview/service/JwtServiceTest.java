package com.pluto.pluto_interview.service;

import com.pluto.pluto_interview.config.properties.JwtProperties;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {
	private String secret = "GzJphLKUH11SInaNgSjLiwSZ2CDO5nuU36WcuqpebBk=";
	private JwtProperties jwtProperties =  Mockito.mock(JwtProperties.class);
	private JwtService service;
	private final String expiredToken = "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsImlhdCI6MTc" +
		  "2NjE2MDM4NCwiZXhwIjoxNzY2MTYwMzg0fQ.fWDjGqY4fz_MisB3Iz88-KlgwdOEY8repq05MrltRQQ";

	@BeforeEach
	void setUp() {
		Mockito.when(jwtProperties.getSecret()).thenReturn(secret);
		service = new JwtService(jwtProperties);
		service = Mockito.spy(service);
	}

	@Test
	void generate_shouldReturnJwtWithSpecifiedTtlAndTimeUnit() {
		Long ttl = 3L;
		TimeUnit timeUnit = TimeUnit.DAYS;
		String issuer = "pluto-interview";
		String jwt = "jwt";
		Map<String, Object> claims = Map.of("userId", 1L);
		Date expiry = Date.from(Instant.now().plus(ttl,
			  timeUnit.toChronoUnit()));

		JwtBuilder jwtBuilder = Mockito.mock(JwtBuilder.class,
			  Mockito.RETURNS_SELF);

		Mockito.when(jwtProperties.getIssuer()).thenReturn(issuer);
		Mockito.when(jwtBuilder.compact()).thenReturn(jwt);

		try (MockedStatic<Jwts> jwts = Mockito.mockStatic(Jwts.class)) {
			jwts.when(Jwts::builder).thenReturn(jwtBuilder);

			ArgumentCaptor<Date> dateCaptor = ArgumentCaptor
				  .forClass(Date.class);

			String token = service.generate(claims, ttl, timeUnit);

			assertNotNull(token);
			assertEquals(jwt, token);

			Mockito.verify(jwtBuilder).claims(claims);
			Mockito.verify(jwtBuilder).issuer(issuer);
			Mockito.verify(jwtBuilder).expiration(dateCaptor.capture());

			Date dateCaptorValue = dateCaptor.getValue();
			assertEquals(expiry, dateCaptorValue);
		}
	}

	@Test
	void generate_shouldReturnJwt() {
		Long ttl = 1L;
		TimeUnit timeUnit = TimeUnit.HOURS;
		String issuer = "pluto-interview";
		String jwt = "jwt";
		Map<String, Object> claims = Map.of("userId", 1L);

		JwtBuilder jwtBuilder = Mockito.mock(JwtBuilder.class, Mockito.RETURNS_SELF);

		Mockito.when(jwtProperties.getTtl()).thenReturn(ttl);
		Mockito.when(jwtProperties.getTimeUnit()).thenReturn(timeUnit);
		Mockito.when(jwtProperties.getIssuer()).thenReturn(issuer);
		Mockito.when(jwtBuilder.compact()).thenReturn(jwt);

		try (MockedStatic<Jwts> jwts = Mockito.mockStatic(Jwts.class)) {
			jwts.when(Jwts::builder).thenReturn(jwtBuilder);
			String token = service.generate(claims, null, null);

			assertNotNull(token);
			assertEquals(jwt, token);

			Mockito.verify(jwtBuilder).claims(claims);
			Mockito.verify(jwtBuilder).issuer(issuer);
		}
	}

	@Test
	void hasExpired_ShouldReturnFalseOnExpiredToken() {
		boolean alive = service.hasExpired(expiredToken);
		assertFalse(alive);
	}

}