package com.pluto.pluto_interview.service;

import com.pluto.pluto_interview.config.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class JwtService implements TokenService{
	private final JwtProperties jwtProperties;
	private final Key key;

	public JwtService(JwtProperties jwtProperties) {
		this.jwtProperties = jwtProperties;
		byte[] secretBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
		key = Keys.hmacShaKeyFor(secretBytes);
	}

	/**
	 * Generate a JWT token with the given claims. The ttl and timeUnit are optional and have default values.
	 * The recommended way to modify them is by specifying them in your application.yaml/properties if needed.
	 * @param claims Claims containing the payload
	 * @param ttl Optional, is 1 by default
	 * @param timeUnit Optional, is HOURS by default
	 * @return The compacted JWT token
	 * @see JwtProperties
	 */
	@Override
	public String generate(Map<String, Object> claims, @Nullable Long ttl, @Nullable TimeUnit timeUnit) {
		Date expiry;
		if (ttl != null && timeUnit != null) {
			expiry = Date.from(Instant.now().plus(ttl, timeUnit.toChronoUnit()));
		} else {
			expiry = Date.from(Instant.now().plus(jwtProperties.getTtl(), jwtProperties.getTimeUnit().toChronoUnit()));
		}

		return Jwts.builder()
			  .claims(claims)
			  .issuer(jwtProperties.getIssuer())
			  .issuedAt(Date.from(Instant.now()))
			  .expiration(expiry)
			  .signWith(key)
			  .compact();
	}

	@Override
	public String generateRefreshToken(Map<String, Object> claims) {
		return Jwts.builder()
			  .claims(claims)
			  .issuer(jwtProperties.getIssuer())
			  .issuedAt(Date.from(Instant.now()))
			  .expiration(Date.from(Instant.now().plus(jwtProperties.getRefreshTokenTtl())))
			  .signWith(key)
			  .compact();
	}

	/**
	 * Parse the given token
	 * @param token The token to parse
	 * @return The claims containing data inside the token
	 */
	@Override
	public Claims parse(String token){
		return getClaims(token);
	}

	@Override
	public boolean hasExpired(String token) {
		try {
			Claims claims = getClaims(token);
			return true;
		} catch (ExpiredJwtException e) {
			return false;
		}
	}

	@Override
	public Date getExpiry(String token) {
		Claims claims = getClaims(token);
		return claims.getExpiration();
	}

	/**
	 * Get the claims from the given token
	 * @param token The token
	 * @return The claims
	 */
	private Claims getClaims(String token){
		return Jwts.parser()
			  .verifyWith((SecretKey) key)
			  .build()
			  .parseSignedClaims(token)
			  .getPayload();
	}
}
