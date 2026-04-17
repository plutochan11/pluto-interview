package com.pluto.pluto_interview.config;

import com.pluto.pluto_interview.filter.JwtFilter;
import com.pluto.pluto_interview.filter.RateLimitingFilter;
import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	@Bean
	public SecurityFilterChain securityFilterChain(
		  HttpSecurity http,
		  JwtFilter jwtFilter,
		  RateLimitingFilter rateLimitingFilter,
		  @Value("${app.cors.allowed-origins}") String allowedOrigins) throws Exception {

		return http.csrf(csrf -> csrf.disable())
			     .cors(cors ->
				       cors.configurationSource(request -> {
					    var corsConfig = new CorsConfiguration();
					    corsConfig.addAllowedOrigin(allowedOrigins);
					    corsConfig.addAllowedMethod("*");
					    corsConfig.addAllowedHeader("*");
					    corsConfig.setAllowCredentials(true);

					    return corsConfig;
			     }))
			     .headers(headers ->
				       headers.xssProtection(xss -> xss.disable())
					         .contentSecurityPolicy(csp ->
						           csp.policyDirectives("script-src 'self'"))
					         .frameOptions(frame -> frame.deny())
					         .httpStrictTransportSecurity(Customizer.withDefaults()))
			     .authorizeHttpRequests(requests ->
				    requests.requestMatchers(
						"/auth/register",
						          "/auth/login",
						          "/auth/refresh-token")
					      .permitAll()
					      .dispatcherTypeMatchers(DispatcherType.ASYNC).permitAll()
					      .anyRequest().authenticated())
			    .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
			    .addFilterBefore(rateLimitingFilter, JwtFilter.class)
			    .build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
