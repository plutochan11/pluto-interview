package com.pluto.pluto_interview.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pluto.pluto_interview.enums.ErrorMessage;
import com.pluto.pluto_interview.exception.IllegalTokenException;
import com.pluto.pluto_interview.exception.UserNotFoundException;
import com.pluto.pluto_interview.exception.UserNotLoggedInException;
import com.pluto.pluto_interview.model.Response;
import com.pluto.pluto_interview.repository.UserRepository;
import com.pluto.pluto_interview.service.AuthenticationService;
import com.pluto.pluto_interview.service.CacheService;
import com.pluto.pluto_interview.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {
	private final JwtService jwtService;
	private final AntPathMatcher pathMatcher = new AntPathMatcher();
	private final ObjectMapper objectMapper;
	private final UserRepository userRepository;
	private final CacheService cacheService;

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		return pathMatcher.match("/auth/**", request.getServletPath());
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
	                                FilterChain filterChain) throws ServletException, IOException {
		// Check whether the request has been authenticated
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated()) {
			filterChain.doFilter(request, response);
			return;
		}

		// Extract JWT token
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

		// Extract user ID and handle expired token
		Claims claims = null;
		try {
			claims = jwtService.parse(jwt);
			if (claims == null || claims.get("userId") == null) {
				throw new IllegalTokenException(ErrorMessage.ILLEGAL_TOKEN.getErrorMessage());
			}
			Long userId = claims.get("userId", Long.class);

			// Validate the token
			String key = AuthenticationService.TOKEN_CACHE_KEY_PREFIX + userId;
			if (!cacheService.containsKey(key)) {
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

			filterChain.doFilter(request, response);
		} catch (ExpiredJwtException e) {
			Response error = Response.error(ErrorMessage.NOT_LOGGED_IN.getErrorMessage());
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			response.getWriter().write(objectMapper.writeValueAsString(error));
			log.info("An user tried to access protected resource with expired token from IP: {}",
				  request.getRemoteAddr());
		}

	}

	private @Nullable String getJwt(HttpServletRequest request){
		String authHeader = request.getHeader("Authorization");
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			return null;
		}
		return authHeader.substring(7);
	}
}
