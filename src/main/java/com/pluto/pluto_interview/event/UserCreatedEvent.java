package com.pluto.pluto_interview.event;

import com.pluto.pluto_interview.model.User;

/**
 * Fired immediately after a new user is successfully registered.
 */
public class UserCreatedEvent extends UserEvent {
	public UserCreatedEvent(Object source, User user) {
		super(source, user);
	}
}
