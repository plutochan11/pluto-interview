package com.pluto.pluto_interview.service;

import com.pluto.pluto_interview.enums.ErrorMessage;
import com.pluto.pluto_interview.mapper.UserMapper;
import com.pluto.pluto_interview.model.Response;
import com.pluto.pluto_interview.model.User;
import com.pluto.pluto_interview.model.vo.UserVo;
import com.pluto.pluto_interview.repository.UserRepository;
import com.pluto.pluto_interview.util.UserIdUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
	private final UserRepository repository;
	private final UserMapper mapper;

	public CompletableFuture<Response> getUser() {
		// Fetch user from database
		Long userId = UserIdUtil.getUserId();
		User user = repository.findById(userId)
			  .orElseThrow(() -> new NoSuchElementException(ErrorMessage.USER_NOT_FOUND.getErrorMessage()));

		// Map to VO
		UserVo vo = mapper.toVo(user);

		// Return
		Response response = Response.ok(vo);
		log.info("User(ID: {}) requested user information.", userId);
		return CompletableFuture.completedFuture(response);
	}
}
