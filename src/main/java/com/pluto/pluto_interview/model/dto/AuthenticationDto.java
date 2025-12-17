package com.pluto.pluto_interview.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthenticationDto(@Email @NotBlank String email, @NotBlank String password) {
}
