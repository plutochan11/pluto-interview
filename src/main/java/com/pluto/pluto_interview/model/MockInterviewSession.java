package com.pluto.pluto_interview.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MockInterviewSession {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private User candidate;

	@OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("createdAt DESC")
	private List<MockInterviewMessage> messages = new ArrayList<>();

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Status status;

	@OneToOne(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
	private MockInterviewResult result;
//	private String result;

	@Column(nullable = false)
	@CreationTimestamp
	private Instant createdAt;

	@UpdateTimestamp
	@Column(nullable = false)
	private Instant updatedAt;

	public enum Status {
		IN_PROGRESS,
		COMPLETED,
		INTERRUPTED,
		CANCELLED
	}

	public String getStatusAsString() {
		return status.toString();
	}

	public static MockInterviewSession newSession() {
		MockInterviewSession session =  new MockInterviewSession();
		session.setStatus(Status.IN_PROGRESS);
		return session;
	}

	public void addMessages(@NotEmpty List<MockInterviewMessage> messages) {
		this.messages.addAll(messages);
	}

	/**
	 * Add the messages to the session's message list and associate the messages with this session.
	 * @param messages
	 */
	public void addMessages(MockInterviewMessage... messages) {
		this.messages.addAll(Arrays.asList(messages));
		for (MockInterviewMessage message : messages) {
			message.setSession(this);
		}
	}
}
