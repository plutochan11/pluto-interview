package com.pluto.pluto_interview.model.dto;

public record AuthenticationResult(String username, String token, Long expiresIn) {
}
