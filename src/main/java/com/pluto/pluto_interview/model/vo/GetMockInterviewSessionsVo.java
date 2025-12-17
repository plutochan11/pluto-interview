package com.pluto.pluto_interview.model.vo;

import com.pluto.pluto_interview.model.dto.Pagination;

import java.util.List;

public record GetMockInterviewSessionsVo(
	  List<MockInterviewSessionVo> sessions,
	  Pagination pagination
) {
}
