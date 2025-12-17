package com.pluto.pluto_interview.model.dto;

import com.pluto.pluto_interview.model.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {
	private String content;
	private String role;
}
