package com.pluto.pluto_interview.model.dto;

import jakarta.validation.constraints.NotBlank;

public record SendMessageDto(@NotBlank String conversationId, @NotBlank String message) {
}
