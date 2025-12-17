package com.pluto.pluto_interview.repository;

import com.pluto.pluto_interview.model.MockInterviewMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MockInterviewMessageRepository extends JpaRepository<MockInterviewMessage, Long> {
	List<MockInterviewMessage> findBySessionIdOrderByCreatedAtAsc(Long sessionId);
	Page<MockInterviewMessage> findBySessionId(Long sessionId, Pageable pageable);
}
