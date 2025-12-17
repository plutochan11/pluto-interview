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
public class MockInterviewResult {
	@Id
	private Long sessionId;

	@OneToOne(fetch = FetchType.LAZY)
	@MapsId
	@JoinColumn(nullable = false)
	private MockInterviewSession session;

	@Column(nullable = false)
	private Double score;

//	@Column(nullable = false)
//	private String duration;
//
//	@Column(nullable = false)
//	private String averageResponseTime;

	@Column(nullable = false)
	private Integer questionAnswered;

	@Lob
	@Column(nullable = false, columnDefinition = "TEXT")
	private String strengths;

	@Lob
	@Column(nullable = false, columnDefinition = "TEXT")
	private String improvements;

	@Lob
	@Column(nullable = false, columnDefinition = "TEXT")
	private String overallFeedback;

	@CreationTimestamp
	private Instant createdAt;
}
