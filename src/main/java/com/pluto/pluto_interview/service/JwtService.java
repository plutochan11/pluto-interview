package com.pluto.pluto_interview.service;

import com.pluto.pluto_interview.config.properties.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JwtService {
	private final JwtProperties jwtProperties;

	public String generateToken(Map<String, Object> claims) {
		return Jwts.builder()
			  .claims(claims)
			  .issuer(jwtProperties.getIssuer())
			  .issuedAt(Date.from(Instant.now()))
			  .expiration(Date.from(Instant.now().plusSeconds(jwtProperties.getTtl())))
			  .signWith(getKey())
			  .compact();
	}

	private Key getKey() {
		byte[] decodedSecret = Decoders.BASE64.decode(jwtProperties.getSecret());
		return Keys.hmacShaKeyFor(decodedSecret);
	}
}
