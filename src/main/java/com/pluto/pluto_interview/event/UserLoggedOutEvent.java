package com.pluto.pluto_interview.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Fired when a user explicitly logs out of the system.
 */
@Getter
public class UserLoggedOutEvent extends ApplicationEvent {
	private final Long userId;

	public UserLoggedOutEvent(Object source, Long userId) {
		super(source);
		this.userId = userId;
	}
}
