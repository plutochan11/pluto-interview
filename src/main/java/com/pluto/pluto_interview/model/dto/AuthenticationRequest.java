package com.pluto.pluto_interview.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthenticationRequest(
	  @Email
	  @NotBlank
	  String email,

	  @NotBlank
//	  @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
	  String password) {
}
