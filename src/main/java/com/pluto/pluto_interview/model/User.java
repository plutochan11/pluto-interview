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
	@SequenceGenerator(name = "user_id_sequence")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_id_sequence")
	private Long id;

	@NotBlank
	private String username;

	@ToString.Exclude
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@NotBlank
	private String password;

	@Email
	@NotBlank
	@Column(unique = true)
	private String email;

	@OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
	private Settings settings;
}
