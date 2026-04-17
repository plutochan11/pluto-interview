package com.pluto.pluto_interview.event;

import com.pluto.pluto_interview.model.User;

/**
 * Fired after a user logs in and tokens are generated.
 */
public class UserLoggedInEvent extends UserEvent {
	public UserLoggedInEvent(Object source, User user) {
		super(source, user);
	}
}
