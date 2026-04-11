package com.pluto.pluto_interview.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Persistable;

import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
// TODO Learn about the plumbing of Persistable and how does Spring Data JPA distinguish between new and existing entities
public class User implements Persistable<Long> {
	@Id
	private Long id;

	@NonNull
	@Column(unique = true, nullable = false)
	private String email;

	@NonNull
	@ToString.Exclude
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@Setter(AccessLevel.NONE)
	@Column(nullable = false)
	private String password;

	@NonNull
	@Column(nullable = false)
	private String username;

	@OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private Settings settings;

	@Setter(AccessLevel.NONE)
	@JsonIgnore
	@OneToMany(mappedBy = "user", orphanRemoval = true)
	private List<Conversation> conversations;

	@Setter(AccessLevel.NONE)
	@JsonIgnore
	@OneToMany(mappedBy = "candidate", orphanRemoval = true)
	private List<MockInterviewSession> mockInterviewSessions;

	@Transient
	@JsonIgnore
	@Builder.Default
	private boolean isNew = true;

	@Override
	public boolean isNew() {
		return isNew;
	}

	@PostLoad
	@PostPersist
	void markNotNew() {
		this.isNew = false;
	}

	public static User newUser(Long id, String email, String password, String username) {
		User user = User.builder()
			  .id(id)
			  .email(email)
			  .password(password)
			  .username(username)
			  .build();

		Settings settings = Settings.createDefault(user);
		user.setSettings(settings);

		return user;
	}
}
