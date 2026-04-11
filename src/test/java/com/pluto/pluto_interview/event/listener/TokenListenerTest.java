package com.pluto.pluto_interview.event.listener;

import com.pluto.pluto_interview.service.CacheService;
import com.pluto.pluto_interview.service.JwtService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class TokenListenerTest {
	@Mock
	private JwtService tokenService;
	@Mock
	private CacheService cacheService;
	@InjectMocks
	private TokenListener tokenListener;
}