package com.pluto.pluto_interview.filter;

import com.pluto.pluto_interview.enums.ErrorMessage;
import com.pluto.pluto_interview.enums.ExceptionMessage;
import com.pluto.pluto_interview.exception.TokenNotFoundException;
import com.pluto.pluto_interview.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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
public class JwtFilter extends OncePerRequestFilter {
	private final TokenService tokenService;
	private final AntPathMatcher pathMatcher = new AntPathMatcher();

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		return pathMatcher.match("/auth/**", request.getServletPath());
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		// Check whether the request has been authenticated
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated()) {
			filterChain.doFilter(request, response);
			return;
		}
		// Extract JWT token
		String authHeader = request.getHeader("Authorization");
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			response.getWriter().write("{\"error\": \"Missing or invalid Authorization header\"}");
			return;
		}
		String token = authHeader.substring(7);
		// Validate token
		if (!tokenService.validate(token)) {
			throw new TokenNotFoundException(ErrorMessage.NOT_LOGGED_IN.getErrorMessage());
		}
		// Set authentication
		Long userId = tokenService.extractUserId(token);
		UsernamePasswordAuthenticationToken auth =
			  new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
		auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
		SecurityContextHolder.getContext().setAuthentication(auth);

		filterChain.doFilter(request, response);
	}
}
