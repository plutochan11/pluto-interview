package com.pluto.pluto_interview.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pluto.pluto_interview.constant.TokenProperty;
import com.pluto.pluto_interview.enums.ErrorMessage;
import com.pluto.pluto_interview.exception.IllegalTokenException;
import com.pluto.pluto_interview.exception.UserNotFoundException;
import com.pluto.pluto_interview.exception.UserNotLoggedInException;
import com.pluto.pluto_interview.model.Response;
import com.pluto.pluto_interview.repository.UserRepository;
import com.pluto.pluto_interview.service.CacheService;
import com.pluto.pluto_interview.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@Slf4j
public class JwtFilter extends OncePerRequestFilter {
	private static final AntPathMatcher pathMatcher = new AntPathMatcher();

	private final JwtService jwtService;
	private final ObjectMapper objectMapper;
	private final UserRepository userRepository;
	private final CacheService cacheService;

	public JwtFilter(JwtService jwtService, ObjectMapper objectMapper, UserRepository userRepository, CacheService cacheService) {
		this.jwtService = jwtService;
		this.objectMapper = objectMapper;
		this.userRepository = userRepository;
		this.cacheService = cacheService;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return pathMatcher.match("/auth/register", request.getServletPath()) ||
			  pathMatcher.match("/auth/login", request.getServletPath()) ||
			  pathMatcher.match("/auth/refresh-token", request.getServletPath());
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
	                                FilterChain filterChain) throws ServletException, IOException {
		if (isAuthenticated(request, response, filterChain)) return;

		String jwt = getJwt(request);

		// Validate token's presence
		if (jwt == null || jwt.isEmpty()) {
			Response error = Response.error(ErrorMessage.ILLEGAL_TOKEN.getErrorMessage());
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			response.getWriter().write(objectMapper.writeValueAsString(error));
			log.info("An unknown user tried to access protected resource without token from IP: {}",
				  request.getRemoteAddr());
			return;
		}

		storeUserId(request, response, jwt);

		filterChain.doFilter(request, response);
	}

	private void storeUserId(HttpServletRequest request, HttpServletResponse response, String jwt) throws IOException {
		Claims claims;
		try {
			claims = jwtService.parse(jwt);
			if (claims == null || claims.get("userId") == null) {
				throw new IllegalTokenException(ErrorMessage.ILLEGAL_TOKEN.getErrorMessage());
			}
			Long userId = claims.get("userId", Long.class);

			// Validate the token
			String key = TokenProperty.TOKEN_KEY_PREFIX + userId;
			String storedToken = cacheService.getString(key);
			if (storedToken == null || !storedToken.equals(jwt)) {
				throw new UserNotLoggedInException(ErrorMessage.NOT_LOGGED_IN.getErrorMessage());
			}

			// Verify user's existence
			userRepository.findById(userId)
				  .orElseThrow(() -> new UserNotFoundException(ErrorMessage.USER_NOT_FOUND.getErrorMessage()));

			// Set authentication
			UsernamePasswordAuthenticationToken auth =
				  new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
			auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
			SecurityContextHolder.getContext().setAuthentication(auth);
		} catch (ExpiredJwtException e) {
			Response error = Response.error(ErrorMessage.NOT_LOGGED_IN.getErrorMessage());
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			response.getWriter().write(objectMapper.writeValueAsString(error));

			log.info("An user tried to access protected resource with expired token from IP: {}",
				  request.getRemoteAddr());

			throw new RuntimeException(e);
		}
	}

	private static boolean isAuthenticated(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated()) {
			filterChain.doFilter(request, response);
			return true;
		}
		return false;
	}

	private @Nullable String getJwt(HttpServletRequest request){
		String authHeader = request.getHeader("Authorization");
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			return null;
		}
		return authHeader.substring(7);
	}
}
