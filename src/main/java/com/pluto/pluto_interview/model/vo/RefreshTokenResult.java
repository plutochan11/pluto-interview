package com.pluto.pluto_interview.model.vo;

import jakarta.validation.constraints.NotBlank;
import org.springframework.lang.Nullable;

public record RefreshTokenResult(
	  @NotBlank String token,
	  @Nullable String refreshToken
) {
}
