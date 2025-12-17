package com.pluto.pluto_interview.service;

import com.pluto.pluto_interview.enums.ExceptionMessage;
import com.pluto.pluto_interview.enums.TokenStatus;
import com.pluto.pluto_interview.exception.TokenAlreadyExpiredException;
import com.pluto.pluto_interview.exception.TokenNotFoundException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.crypto.SecretKey;
import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;

@Service
public class TokenService {
	private final StringRedisTemplate redisTemplate;
	private final SecretKey secretKey;

	public TokenService(StringRedisTemplate redisTemplate, @Value("${jwt.secret}") String secret) {
		this.redisTemplate = redisTemplate;
		byte[] secretBytes = Decoders.BASE64.decode(secret);
		secretKey = Keys.hmacShaKeyFor(secretBytes);
	}

	public void save(String token) {
		try {
			Claims claims = extractClaimsFrom(token);
			String jti = claims.getId();

			Date expiration = claims.getExpiration();
			long expEpoch = expiration != null ? expiration.toInstant().getEpochSecond()
				  : Instant.now().getEpochSecond();
			long ttlSeconds = expEpoch - Instant.now().getEpochSecond();
			if (ttlSeconds <= 0) {
				throw new TokenAlreadyExpiredException(
					  ExceptionMessage.TOKEN_ALREADY_EXPIRED.getMessage());
			}

			String key = jti != null ? "token:jti:" + jti : "token:token:" + Integer.toHexString(token.hashCode());
			redisTemplate.opsForValue().set(key, TokenStatus.VALID.getCode(), Duration.ofSeconds(ttlSeconds));
		} catch (JwtException | IllegalArgumentException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Invalidate the given token and return the user ID in it.
	 * @param token
	 * @return the user ID in the token
	 */
	public String invalidate(String token) {
		Claims claims = extractClaimsFrom(token);
		String jti = claims.getId();
		String key = jti != null ? "token:jti:" + jti : "token:token:" + Integer.toHexString(token.hashCode());
		if (!redisTemplate.delete(key)) {
			throw new TokenNotFoundException(ExceptionMessage.TOKEN_NOT_FOUND.getMessage());
		}
		return String.valueOf(claims.get("userId", Long.class));
	}

	public boolean validate(String token) {
		Claims claims = extractClaimsFrom(token);
		String jti = claims.getId();
		String key = jti != null ? "token:jti:" + jti : "token:token:" + Integer.toHexString(token.hashCode());
		return Objects.equals(TokenStatus.VALID.getCode(), redisTemplate.opsForValue().get(key));
	}

	public Long extractUserId(String token) {
		Claims claims = extractClaimsFrom(token);
		return claims.get("userId", Long.class);
	}

	private Claims extractClaimsFrom(String token) {
		return Jwts.parser()
			  .verifyWith(secretKey)
			  .build()
			  .parseSignedClaims(token)
			  .getPayload();
	}
}
