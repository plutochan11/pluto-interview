package com.pluto.pluto_interview.model;

import jakarta.validation.constraints.NotNull;

public record Response(@NotNull boolean success, String message, Object data, Object error) {
	public static Response ok() {
		return new Response(true, null, null, null);
	}

	public static Response ok(Object data) {
		return new Response(true, null, data, null);
	}

	public static Response error(String message) {
		return new Response(false, message, null, null);
	}
}
