package com.pluto.pluto_interview.model.vo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record UserVo(
	  @NotNull
	  Long id,

	  @NotBlank
	  @Email
	  String email,

	  @NotBlank
	  String username
) {
}
