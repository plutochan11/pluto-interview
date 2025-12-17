package com.pluto.pluto_interview.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MockInterviewMessage {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Role role;

	private Long responseTime;

	@CreationTimestamp
	private Instant createdAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private MockInterviewSession session;

	enum Role {
		INTERVIEWER,
		CANDIDATE
	}

	public String getRoleAsString() {
		return role.toString();
	}

	public static MockInterviewMessage fromCandidate(String text) {
		return MockInterviewMessage.builder()
			  .content(text)
			  .role(Role.CANDIDATE)
			  .build();
	}

	public static MockInterviewMessage fromInterviewer(String text) {
		return MockInterviewMessage.builder()
			  .content(text)
			  .role(Role.INTERVIEWER)
			  .build();
	}
}
