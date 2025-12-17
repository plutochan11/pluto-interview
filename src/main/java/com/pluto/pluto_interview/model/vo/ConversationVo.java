package com.pluto.pluto_interview.model.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ConversationVo(@NotNull Long conversationId, @NotBlank String title) {
}
