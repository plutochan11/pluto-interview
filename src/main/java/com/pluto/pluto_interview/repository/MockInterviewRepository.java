package com.pluto.pluto_interview.repository;

import com.pluto.pluto_interview.model.MockInterviewSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MockInterviewRepository extends JpaRepository<MockInterviewSession, Long> {
	Page<MockInterviewSession> findByCandidateId(Long candidateId, Pageable pageable);
}
