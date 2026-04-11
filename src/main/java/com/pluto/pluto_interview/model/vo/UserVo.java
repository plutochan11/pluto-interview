package com.pluto.pluto_interview.model.vo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.NonNull;

public record UserVo(
	  @NonNull
	  Long id,

	  @NonNull
	  String email,

	  @NonNull
	  String username
) {
}
