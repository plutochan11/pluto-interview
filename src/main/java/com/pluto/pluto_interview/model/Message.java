package com.pluto.pluto_interview.model;

import dev.langchain4j.data.message.*;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private Conversation conversation;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Role role;

	@Column(columnDefinition = "TEXT", nullable = false)
	private String content;

	public enum Role {
		USER,
		ASSISTANT
	}

	/**
	 * Create a Message from an AI message string
	 * @param text
	 * @return
	 */
	public static Message fromAi(String text) {
		Message message = new Message();
		message.setContent(text);
		message.setRole(Role.ASSISTANT);
		return message;
	}

	/**
	 * Create a Message from a user message string
	 * @param text
	 * @return
	 */
	public static Message fromUser(String text) {
		Message message = new Message();
		message.setContent(text);
		message.setRole(Role.USER);
		return message;
	}
}
