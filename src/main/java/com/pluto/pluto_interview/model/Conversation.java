package com.pluto.pluto_interview.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
//@Builder
@NoArgsConstructor
//@AllArgsConstructor
public class Conversation {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private User user;

	@OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Message> messages = new ArrayList<>();

	@CreationTimestamp
	@Column(nullable = false)
	private Instant createdAt;

//	@UpdateTimestamp
	@Column(nullable = false)
	private Instant updatedAt = Instant.now();

	public Conversation(Long id, String title, User user, List<Message> messages) {
		this.id = id;
		this.title = title;
		this.user = user;
		this.messages = new ArrayList<>();
		if (messages != null && !messages.isEmpty()) {
			this.messages.addAll(messages);
		}
	}

	public void addMessage(Message message) {
		messages.add(message);
		message.setConversation(this);
	}
}
