package com.pluto.pluto_interview.util;

import com.pluto.pluto_interview.enums.ErrorMessage;
import com.pluto.pluto_interview.enums.ExceptionMessage;
import com.pluto.pluto_interview.exception.UserNotLoggedInException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class UserIdUtil {
	public static Long getUserId() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated()) {
			throw new UserNotLoggedInException(ErrorMessage.NOT_LOGGED_IN.getErrorMessage());
		}
		return (Long) auth.getPrincipal();
	}
}
