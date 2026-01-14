package com.pluto.pluto_interview.model.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthenticationResult(@NotBlank String username, @NotBlank String token,
                                   @NotBlank String refreshToken) {
}
