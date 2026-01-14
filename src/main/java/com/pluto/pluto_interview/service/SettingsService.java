package com.pluto.pluto_interview.service;

import com.pluto.pluto_interview.enums.DifficultyLevel;
import com.pluto.pluto_interview.enums.ErrorMessage;
import com.pluto.pluto_interview.enums.QuestionType;
import com.pluto.pluto_interview.exception.IllegalDifficultyLevelException;
import com.pluto.pluto_interview.exception.IllegalQuestionTypeException;
import com.pluto.pluto_interview.exception.UserNotFoundException;
import com.pluto.pluto_interview.model.Question;
import com.pluto.pluto_interview.model.Response;
import com.pluto.pluto_interview.model.Settings;
import com.pluto.pluto_interview.model.dto.SettingsDto;
import com.pluto.pluto_interview.model.dto.SettingsResponse;
import com.pluto.pluto_interview.repository.SettingsRepository;
import com.pluto.pluto_interview.util.UserIdUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettingsService {
	private final SettingsRepository settingsRepository;

	public Response getSettings() {
		// Get user ID from security context
		Long userId = UserIdUtil.getUserId();
		// Fetch settings from database
		Settings settings = settingsRepository.findById(userId).orElseThrow(() ->
			  new UserNotFoundException(ErrorMessage.USER_NOT_FOUND.getErrorMessage(),
				    new Throwable("Non-existing user ID: " + userId))
		);
		// Assemble Response and return
		List<String> interviewTypes = Arrays.stream(QuestionType.values())
			  .map(QuestionType::getQuestionType)
			  .toList();
		List<String> difficultyLevels = Arrays.stream(DifficultyLevel.values())
			  .map(DifficultyLevel::getDifficultyLevel)
			  .toList();
		SettingsResponse settingsResponse = new SettingsResponse(interviewTypes,
			  settings.getPreferredQuestionTypes().stream()
				    .map(Enum::toString)
				    .toList(),
			  difficultyLevels,
			  settings.getPreferredDifficultyLevel().toString());
		return Response.ok(settingsResponse);
	}

	public Response adjustSettings(@Validated SettingsDto settingsDto) {
		// Get user ID, verify settings existence and assemble new settings
		Long userId = UserIdUtil.getUserId();
		Settings settings = settingsRepository.findById(userId).orElseThrow(() ->
			  new UserNotFoundException(ErrorMessage.USER_NOT_FOUND.getErrorMessage()));

		if (settingsDto.preferredQuestionTypes() != null) {
			List<Question.QuestionType> preferredQuestionTypes = verifyAndConvertToPreferredQuestionTypes(settingsDto);
			settings.adjustPreferredQuestionTypes(preferredQuestionTypes);
		} else {
			settings.adjustPreferredQuestionTypes(null);
		}

		if (settingsDto.preferredDifficultyLevel() == null) {
			throw new IllegalDifficultyLevelException(ErrorMessage.ILLEGAL_DIFFICULTY_LEVEL.getErrorMessage());
		}
		Question.DifficultyLevel preferredDifficultyLevel = verifyAndConvertToPreferredDifficultyLevel(settingsDto);
		settings.setPreferredDifficultyLevel(preferredDifficultyLevel);

		// Save the new settings and assemble response
		Settings savedSettings = settingsRepository.save(settings);
		SettingsResponse settingsResponse = getSettingsResponseFrom(savedSettings);

		log.info("User (ID: {}) updated their settings (Arguments: {}).", userId, settingsDto);
		return Response.ok(settingsResponse);
	}

	private static SettingsResponse getSettingsResponseFrom(Settings savedSettings) {
		SettingsResponse settingsResponse = new SettingsResponse(
			  Arrays.stream(Question.QuestionType.values())
				    .map(Enum::toString)
				    .toList(),
			  savedSettings.getPreferredQuestionTypes().stream()
				    .map(Enum::toString)
				    .toList(),
			  Arrays.stream(Question.DifficultyLevel.values())
				    .map(Enum::toString)
				    .toList(),
			  savedSettings.getPreferredDifficultyLevel().toString()
		);
		return settingsResponse;
	}

	private static Question.DifficultyLevel verifyAndConvertToPreferredDifficultyLevel(
		  SettingsDto settingsDto) {
		return Arrays.stream(Question.DifficultyLevel.values())
			  .filter(dl -> settingsDto.preferredDifficultyLevel()
				    .equalsIgnoreCase(dl.toString()))
			  .findFirst()
			  .orElseThrow(() ->
				    new IllegalDifficultyLevelException(ErrorMessage
					      .ILLEGAL_DIFFICULTY_LEVEL.getErrorMessage()));
	}

	private static List<Question.QuestionType> verifyAndConvertToPreferredQuestionTypes(SettingsDto settingsDto) {
		return settingsDto.preferredQuestionTypes().stream()
			  .map(pqt -> Arrays.stream(Question.QuestionType.values())
				    .filter(qt ->
						qt.toString().equalsIgnoreCase(pqt))
				    .findFirst()
				    .orElseThrow(() ->
						new IllegalQuestionTypeException(ErrorMessage.ILLEGAL_QUESTION_TYPE.getErrorMessage())))
			  .toList();
	}
}
