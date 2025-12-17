package com.pluto.pluto_interview.repository;

import com.pluto.pluto_interview.model.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
	List<Conversation> findByUserIdOrderByUpdatedAtDesc(Long userId);

	Page<Conversation> findByUserId(Long userId, Pageable pageable);

	@Query("""
		select distinct c
		from Conversation c
		left join c.messages m
		where c.user.id = :userId
			and (
				lower(c.title) like lower(concat('%', :query, '%'))
				or lower(m.content) like lower(concat('%', :query, '%'))
			)
	""")
	Page<Conversation> findByUserIdAndQuery(@Param("userId") Long userId,
	                                        @Param("query") String query,
	                                        Pageable pageable);
}
