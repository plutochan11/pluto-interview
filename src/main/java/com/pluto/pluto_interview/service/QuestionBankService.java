package com.pluto.pluto_interview.service;

import com.pluto.pluto_interview.enums.DifficultyLevel;
import com.pluto.pluto_interview.enums.ErrorMessage;
import com.pluto.pluto_interview.exception.IllegalDifficultyLevelException;
import com.pluto.pluto_interview.exception.IllegalQuestionTypeException;
import com.pluto.pluto_interview.exception.QuestionNotFoundException;
import com.pluto.pluto_interview.mapper.QuestionMapper;
import com.pluto.pluto_interview.model.Question;
import com.pluto.pluto_interview.model.QuestionType;
import com.pluto.pluto_interview.model.Response;
import com.pluto.pluto_interview.model.dto.*;
import com.pluto.pluto_interview.model.vo.GetQuestionByIdVo;
import com.pluto.pluto_interview.model.vo.QuestionTypeVo;
import com.pluto.pluto_interview.model.vo.QuestionTypesVo;
import com.pluto.pluto_interview.model.vo.QuestionsVo;
import com.pluto.pluto_interview.repository.QuestionRepository;
import com.pluto.pluto_interview.repository.QuestionTypeRepository;
import com.pluto.pluto_interview.util.UserIdUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class QuestionBankService {
	private final QuestionRepository questionRepo;
	private final QuestionTypeRepository questionTypeRepo;
	private final ApplicationEventPublisher eventPublisher;
	private final QuestionMapper questionMapper;
	private final EmbeddingService embeddingService;

//	public Response getQuestions(@Validated QuestionBankDto questionBankDto) {
//		// Create a PageRequest object
//		PageRequest pageRequest = PageRequest.of(questionBankDto.getOffset(),
//			  questionBankDto.getLimit());
//
//		// Construct specifications based on the filters provided in questionBankDto
//		Specification<Question> spec = assembleQuestionSpecification(questionBankDto);
//
//		// Fetch filtered and paginated questions
////		List<Question> questions = questionRepository.findAll(spec, pageRequest).getContent();
//		Page<Question> questions = questionRepo.findAll(spec, pageRequest);
//		Pagination pagination = new Pagination(questions.getTotalElements(), questions.getTotalPages(),
//			  questionBankDto.getOffset(), questionBankDto.getLimit()
//			  );
//		List<QuestionResult> questionResults = questions.getContent().stream()
//			  .map(question -> new QuestionResult(question.getId(),
//				    question.getTitle(), question.getQuestionType(),
//				    question.getDifficultyLevel(), question.getFromCompany()))
//			  .toList();
//		QuestionsVo questionsVo = new QuestionsVo(questionResults, pagination);
//		// Return a Response object
//		return Response.ok(questionsVo);
//	}

	@Async
	public CompletableFuture<Response> getQuestions(Integer offset, Integer limit, String query, String questionType,
	                                                String difficultyLevel) {
		// Verify question type and difficulty level
		validateQuestionType(questionType);
		validateDifficultyLevel(difficultyLevel);

		// Create Specification as per query, questionType and difficultyLevel
		Specification<Question> specification = createSpecification(query, questionType, difficultyLevel);

		// Create Pageable
		Pageable pageable = PageRequest.of(offset, limit, Sort.Direction.DESC, "createdAt");

		// Fetch entries from the DB
		Page<Question> questionPage = questionRepo.findAll(specification, pageable);
		List<Question> questions = questionPage.getContent();

		// Create VO
		List<QuestionResult> questionResults = questions.stream()
			  .map(q -> new QuestionResult(q.getId(), q.getTitle(), q.getQuestionType().toString(),
				    q.getDifficultyLevel().toString(), q.getFromCompany()))
			  .toList();
		Pagination pagination = new Pagination(questionPage.getTotalElements(), questionPage.getTotalPages(),
			  offset, limit);
		QuestionsVo vo = new QuestionsVo(questionResults, pagination);

		// Return a completed CompletableFuture
		Response response = Response.ok(vo);
		return CompletableFuture.completedFuture(response);
	}

	public Response getQuestionTypes() {
		// Fetch QuestionType records from the database
		List<QuestionType> questionTypes = questionTypeRepo.findAll();

		// Convert them to a list of QuestionTypeVo objects
		List<QuestionTypeVo> questionTypeVos = questionTypes.stream()
			  .map(qt -> new QuestionTypeVo(String.valueOf(qt.getId()),
				    qt.getName(), qt.getDescription(), qt.getQuestionCount()))
			  .toList();

		// Populate a QuestionTypesVo and return a Response object
		QuestionTypesVo questionTypesVo = new QuestionTypesVo(questionTypeVos);
		return Response.ok(questionTypesVo);
	}

//	private static Specification<Question> assembleQuestionSpecification(
//		  QuestionBankDto questionBankDto) {
//		Specification<Question> spec = Specification.unrestricted();
//		String query = questionBankDto.getQuery();
//		if (query != null && !query.isBlank()) {
//			String likeClause = "%" + query.toLowerCase() + "%";
//			spec = spec.and((root, cq,
//			                 cb) ->
//				  cb.like(cb.lower(root.get("title")), likeClause)
//			);
//		}
//		String questionType = questionBankDto.getQuestionType();
//		if (questionType != null && !questionType.isBlank()) {
//			spec = spec.and((root, cq,
//			                 cb) ->
//				  cb.equal(root.get("questionType"), questionType));
//		}
//		String difficultyLevel = questionBankDto.getDifficultyLevel();
//		if (difficultyLevel != null && !difficultyLevel.isBlank()) {
//			spec = spec.and((root, cq,
//			                 cb) ->
//				  cb.equal(root.get("difficultyLevel"), difficultyLevel));
//		}
//		return spec;
//	}

	@Transactional
	public Response addQuestion(@Valid QuestionDto questionDto) {
		// Verify QuestionType and DifficultyLevel
		verifyQuestionType(questionDto);
		verifyDifficultyLevel(questionDto);

		// Create a Question object from the DTO
		Question.QuestionType questionType = getQuestionTypeFrom(questionDto.questionType());
		Question.DifficultyLevel difficultyLevel = getDifficultyLevelFrom(questionDto.difficultyLevel());
		Question question = Question.builder()
			  .title(questionDto.title())
			  .content(questionDto.content())
			  .answer(questionDto.answer())
			  .questionType(questionType)
			  .difficultyLevel(difficultyLevel)
			  .fromCompany(questionDto.from())
			  .build();

		// Persist
		Question savedQuestion = questionRepo.save(question);

		// Embed answer and store
		embeddingService.embedAndStore(savedQuestion.getAnswer(), savedQuestion.getId().toString());

		// Publish a QuestionAddedEvent
//		eventPublisher.publishEvent(new QuestionAddedEvent(this, question));

		// Return a Response object with success
		return Response.ok();
	}

	private static void verifyDifficultyLevel(QuestionDto questionDto) {
		Arrays.stream(DifficultyLevel.values())
			  .filter(difficultyLevel ->
				    difficultyLevel.getDifficultyLevel()
					      .equalsIgnoreCase(questionDto.difficultyLevel()))
			  .findFirst()
			  .orElseThrow(() ->
				    new IllegalDifficultyLevelException(
						ErrorMessage.ILLEGAL_DIFFICULTY_LEVEL
							  .getErrorMessage()));
	}

	private static void verifyQuestionType(QuestionDto questionDto) {
		Arrays.stream(com.pluto.pluto_interview.enums.QuestionType.values())
			  .filter(enumQuestionType ->
				    enumQuestionType.getQuestionType()
					      .equalsIgnoreCase(questionDto.questionType()))
			  .findFirst()
			  .orElseThrow(() ->
				    new IllegalQuestionTypeException(
						ErrorMessage.ILLEGAL_QUESTION_TYPE
							  .getErrorMessage()));
	}

	public CompletableFuture<Response> getQuestionById(Long questionId) {
		// Find question by ID
		Question question = questionRepo.findById(questionId)
			  .orElseThrow(() -> new QuestionNotFoundException(ErrorMessage.QUESTION_NOT_FOUND.getErrorMessage()));

		// Map to VO
		GetQuestionByIdVo vo = questionMapper.toGetQuestionByIdVo(question);

		// Create and return completed future
		return CompletableFuture.completedFuture(Response.ok(vo));
	}

	private void validateDifficultyLevel(String difficultyLevel) {
		if (difficultyLevel != null && !difficultyLevel.isBlank()) {
			boolean isValidDifficultyLevel = Arrays.stream(Question.DifficultyLevel.values())
				  .anyMatch(dl -> dl.toString()
					    .equalsIgnoreCase(difficultyLevel));
			if (!isValidDifficultyLevel) {
				throw new IllegalArgumentException(String
					  .format("Invalid difficulty level: %s;" +
						    "User(%d)", difficultyLevel, UserIdUtil.getUserId()));
			}
		}
	}

	private void validateQuestionType(String questionType) {
		if (questionType != null && !questionType.isBlank()) {
			boolean isValidQuestionType = Arrays.stream(Question.QuestionType.values())
				  .anyMatch(qt -> qt.toString()
					    .equalsIgnoreCase(questionType));
			if (!isValidQuestionType) {
				throw new IllegalArgumentException(String
					  .format("Invalid question type: %s;" +
						    "User(%d)", questionType, UserIdUtil.getUserId()));
			}
		}
	}

	private Specification<Question> createSpecification(String query, String questionType, String difficultyLevel) {
		Specification<Question> specification = Specification.unrestricted();
		if (query != null && !query.isBlank()) {
			String likePattern = "%" + query + "%";
			specification = specification
				  .and((root, cq, cb) ->
					    cb.like(root.get("title"), likePattern))
				  .or((root, cq, cb) ->
					    cb.like(root.get("content"), likePattern));
		}
		if (questionType != null && !questionType.isBlank()) {
			specification = specification.and((root, cq, cb) ->
				  cb.equal(root.get("questionType"), questionType));
		}
		if (difficultyLevel != null && !difficultyLevel.isBlank()) {
			specification = specification.and((root, cq, cb) ->
				  cb.equal(root.get("difficultyLevel"), difficultyLevel));
		}
		return specification;
	}

	private static Question.DifficultyLevel getDifficultyLevelFrom(String difficultyLevelString) {
		return Arrays.stream(Question.DifficultyLevel.values())
			  .filter(dl -> difficultyLevelString.equalsIgnoreCase(dl.toString()))
			  .findFirst()
			  .orElseThrow(() -> new IllegalArgumentException(ErrorMessage.ILLEGAL_DIFFICULTY_LEVEL.getErrorMessage()));
	}

	private static Question.QuestionType getQuestionTypeFrom(String questionTypeString) {
		return Arrays.stream(Question.QuestionType.values())
			  .filter(qt -> questionTypeString.equalsIgnoreCase(qt.toString()))
			  .findFirst()
			  .orElseThrow(() -> new IllegalArgumentException(ErrorMessage.ILLEGAL_QUESTION_TYPE.getErrorMessage()));
	}
}
