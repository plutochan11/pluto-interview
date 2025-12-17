package com.pluto.pluto_interview.service;

import com.pluto.pluto_interview.mapper.MockInterviewResultMapper;
import com.pluto.pluto_interview.model.MockInterviewResult;
import com.pluto.pluto_interview.model.dto.MockInterviewResultDto;
import com.pluto.pluto_interview.repository.MockInterviewResultRepository;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MockInterviewEvaluationService {
	private final MockInterviewEvaluator evaluator;
	private final MockInterviewResultRepository resultRepository;
	private final MockInterviewResultMapper resultMapper;

	@Transactional
	public MockInterviewResult evaluate(String chatHistory) {
		// Call the LLM to generate the structured evaluation.
		MockInterviewResultDto evaluationDto = evaluator.evaluate(chatHistory);

		// Map DTO to Entity
		return MockInterviewResult.builder()
			  .score(evaluationDto.score())
			  .questionAnswered(evaluationDto.questionAnswered())
			  .strengths(evaluationDto.strengths())
			  .improvements(evaluationDto.improvements())
			  .overallFeedback(evaluationDto.overallFeedback())
			  .build();
	}
}
