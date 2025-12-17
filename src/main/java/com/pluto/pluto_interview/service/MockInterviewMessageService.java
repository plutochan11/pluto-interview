package com.pluto.pluto_interview.service;

import com.pluto.pluto_interview.model.MockInterviewMessage;
import com.pluto.pluto_interview.repository.MockInterviewMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MockInterviewMessageService {
	private final MockInterviewMessageRepository repository;

	public Page<MockInterviewMessage> getMessages(Long sessionId, Pageable pageable) {
		return repository.findBySessionId(sessionId, pageable);
	}
}
