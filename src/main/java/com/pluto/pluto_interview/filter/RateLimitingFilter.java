package com.pluto.pluto_interview.filter;

import com.pluto.pluto_interview.enums.ErrorMessage;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

@Component
public class RateLimitingFilter implements Filter {
	private final Bucket bucket;

	public RateLimitingFilter(
		  @Value("${rate-limiting.capacity}") long capacity,
		  @Value("${rate-limiting.refill-amount}") long refillAmount,
		  @Value("${rate-limiting.refill-period}") Duration refillPeriod
	) {

		Bandwidth limit = Bandwidth.builder()
			                     .capacity(capacity)
			                     .refillGreedy(refillAmount, refillPeriod)
			                     .build();
		this.bucket = Bucket.builder()
			              .addLimit(limit)
			              .build();
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
		if (bucket.tryConsume(1)) {
			filterChain.doFilter(servletRequest, servletResponse);
		} else {
			HttpServletResponse response = (HttpServletResponse) servletResponse;
			response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
			response.getWriter().write(ErrorMessage.RATE_LIMIT_EXCEEDED.getErrorMessage());
		}
	}
}
