package com.pluto.pluto_interview.repository;

import com.pluto.pluto_interview.model.MockInterviewResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MockInterviewResultRepository extends JpaRepository<MockInterviewResult, Long> {
}
