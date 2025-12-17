package com.pluto.pluto_interview.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pagination {
	@NotNull
	private Long total;
	@NotNull
	private Integer pageCount;
	@NotNull
	private Integer offset;
	@NotNull
	private Integer limit;
}
