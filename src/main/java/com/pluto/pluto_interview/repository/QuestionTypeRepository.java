package com.pluto.pluto_interview.repository;

import com.pluto.pluto_interview.model.QuestionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuestionTypeRepository extends JpaRepository<QuestionType, Long> {
	Optional<QuestionType> findByName(String name);
}
