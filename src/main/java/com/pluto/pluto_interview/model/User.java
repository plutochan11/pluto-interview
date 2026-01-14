package com.pluto.pluto_interview.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
	@Id
	@SequenceGenerator(name = "user_id_sequence", sequenceName = "user_id_sequence", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_id_sequence")
	private Long id;

	@NonNull
	@Column(unique = true, nullable = false)
	private String email;

	@NonNull
	@ToString.Exclude
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@Column(nullable = false)
	private String password;

	@NonNull
	@Column(nullable = false)
	private String username;

	@OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private Settings settings;

	public static User newUser(String email, String password, String username) {
		User user = User.builder()
			  .email(email)
			  .password(password)
			  .username(username)
			  .build();
		Settings settings = Settings.withDefault(user);
		user.setSettings(settings);

		return user;
	}
}
