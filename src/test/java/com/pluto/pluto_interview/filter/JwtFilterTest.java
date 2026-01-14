package com.pluto.pluto_interview.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pluto.pluto_interview.exception.IllegalTokenException;
import com.pluto.pluto_interview.exception.UserNotFoundException;
import com.pluto.pluto_interview.model.User;
import com.pluto.pluto_interview.repository.UserRepository;
import com.pluto.pluto_interview.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {
	@Mock
	private JwtService jwtService;
	@Mock
	private AntPathMatcher pathMatcher;
	@Mock
	private ObjectMapper objectMapper;
	@Mock
	private UserRepository userRepository;

	HttpServletRequest request = mock(HttpServletRequest.class);
	HttpServletResponse response = mock(HttpServletResponse.class);
	FilterChain filterChain = mock(FilterChain.class);
	Authentication authentication = mock(Authentication.class);
	SecurityContext securityContext = mock(SecurityContext.class);

	@InjectMocks
	private JwtFilter jwtFilter;

	@Test
	void doFilterInternal_shouldReturn_whenAuthenticationIsAuthenticated() throws ServletException, IOException {
		when(authentication.isAuthenticated()).thenReturn(true);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		try (MockedStatic<SecurityContextHolder> securityContextHolder =
			       Mockito.mockStatic(SecurityContextHolder.class)) {
			securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

			jwtFilter.doFilterInternal(request, response, filterChain);

			verify(filterChain).doFilter(request, response);
			verifyNoInteractions(jwtService);
			verifyNoInteractions(userRepository);
		}
	}

	@Test
	void doFilterInternal_shouldReturnErrorResponse_whenJwtIsAbsent() throws IOException, ServletException {
		String jwtHeader = "Authorization";
		when(authentication.isAuthenticated()).thenReturn(false);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(request.getHeader(jwtHeader)).thenReturn(null);

		try (MockedStatic<SecurityContextHolder> securityContextHolder =
			  Mockito.mockStatic(SecurityContextHolder.class);
			PrintWriter writer = mock(PrintWriter.class)) {
			securityContextHolder.when(SecurityContextHolder::getContext)
				  .thenReturn(securityContext);
			when(response.getWriter()).thenReturn(writer);

			jwtFilter.doFilterInternal(request, response, filterChain);

			verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			verifyNoInteractions(jwtService, userRepository, filterChain);
		}
	}

	@Test
	void doFilterInternal_shouldThrowIllegalTokenException_whenJwtIsIllegal() {
		String jwtHeader = "Authorization";
		String jwtHeaderValue = "Bearer jwt token";
		String jwt = "jwt token";
		Claims claims = mock(Claims.class);
		String key = "userId";
		when(authentication.isAuthenticated()).thenReturn(false);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(request.getHeader(jwtHeader)).thenReturn(jwtHeaderValue);
		when(jwtService.parse(jwt)).thenReturn(claims);
		when(claims.get(key)).thenReturn(null);

		assertThrows(IllegalTokenException.class, () -> {
			try (MockedStatic<SecurityContextHolder> securityContextHolder =
				       Mockito.mockStatic(SecurityContextHolder.class)) {
				securityContextHolder.when(SecurityContextHolder::getContext)
					  .thenReturn(securityContext);

				jwtFilter.doFilterInternal(request, response, filterChain);
			}
		});

		verifyNoInteractions(userRepository, filterChain);
	}

	@Test
	void doFilterInternal_shouldThrowUserNotFoundException_whenUserIdNonExisting() {
		String jwtHeader = "Authorization";
		String jwtHeaderValue = "Bearer jwt token";
		String jwt = "jwt token";
		Claims claims = mock(Claims.class);
		String key = "userId";
		Long userId = 1L;
		when(authentication.isAuthenticated()).thenReturn(false);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(request.getHeader(jwtHeader)).thenReturn(jwtHeaderValue);
		when(jwtService.parse(jwt)).thenReturn(claims);
		when(claims.get(key)).thenReturn(userId);
		when(claims.get(key, Long.class)).thenReturn(userId);
		when(userRepository.findById(userId)).thenReturn(Optional.empty());

		assertThrows(UserNotFoundException.class, () -> {
			try (MockedStatic<SecurityContextHolder> securityContextHolder =
				       Mockito.mockStatic(SecurityContextHolder.class)) {
				securityContextHolder.when(SecurityContextHolder::getContext)
					  .thenReturn(securityContext);

				jwtFilter.doFilterInternal(request, response, filterChain);
			}
		});

		verifyNoInteractions(filterChain);
	}

	@Test
	void doFilterInternal_shouldSetAuthenticationAndProceedFilterChain_whenJwtIsValid() throws ServletException, IOException {
		String jwtHeader = "Authorization";
		String jwtHeaderValue = "Bearer jwt token";
		String jwt = "jwt token";
		Claims claims = mock(Claims.class);
		String key = "userId";
		Long userId = 1L;
		User user = new User();
		when(authentication.isAuthenticated()).thenReturn(false);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(request.getHeader(jwtHeader)).thenReturn(jwtHeaderValue);
		when(jwtService.parse(jwt)).thenReturn(claims);
		when(claims.get(key)).thenReturn(userId);
		when(claims.get(key, Long.class)).thenReturn(userId);
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));

		try (MockedStatic<SecurityContextHolder> securityContextHolder =
			       Mockito.mockStatic(SecurityContextHolder.class)) {
			securityContextHolder.when(SecurityContextHolder::getContext)
				  .thenReturn(securityContext);

			jwtFilter.doFilterInternal(request, response, filterChain);

			ArgumentCaptor<UsernamePasswordAuthenticationToken>
				  authTokenCaptor = ArgumentCaptor.forClass(
					    UsernamePasswordAuthenticationToken.class
			);
			verify(securityContext).setAuthentication(authTokenCaptor.capture());
			UsernamePasswordAuthenticationToken token = authTokenCaptor.getValue();

			assertEquals(userId, token.getPrincipal());
			assertNull(token.getCredentials());
			assertTrue(token.getAuthorities().isEmpty());

			verify(filterChain).doFilter(request, response);
		}
	}
}