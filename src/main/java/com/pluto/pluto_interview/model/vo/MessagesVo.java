package com.pluto.pluto_interview.model.vo;

import jakarta.validation.constraints.NotBlank;

public record MessagesVo(@NotBlank String content, @NotBlank String role) {
}
