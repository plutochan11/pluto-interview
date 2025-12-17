package com.pluto.pluto_interview.model.dto;

import jakarta.validation.constraints.NotNull;

public record DeleteConversationDto(@NotNull Long conversationId) {
}
