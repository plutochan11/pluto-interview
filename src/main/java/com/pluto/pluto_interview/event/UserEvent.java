package com.pluto.pluto_interview.event;

import com.pluto.pluto_interview.model.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Base event published when an action happens to a user entity.
 */
@Getter
public abstract class UserEvent extends ApplicationEvent {
	private final User user;

	public UserEvent(Object source, User user) {
		super(source);
		this.user = user;
	}
}
