package com.pluto.pluto_interview.service;

import com.pluto.pluto_interview.enums.ErrorMessage;
import com.pluto.pluto_interview.exception.IllegalDifficultyLevelException;
import com.pluto.pluto_interview.exception.IllegalQuestionTypeException;
import com.pluto.pluto_interview.listener.UserListener;
import com.pluto.pluto_interview.mapper.QuestionTypeMapper;
import com.pluto.pluto_interview.model.Question;
import com.pluto.pluto_interview.model.Response;
import com.pluto.pluto_interview.model.Settings;
import com.pluto.pluto_interview.model.dto.NewSettings;
import com.pluto.pluto_interview.model.dto.SettingsResponse;
import com.pluto.pluto_interview.model.vo.SettingsDetail;
import com.pluto.pluto_interview.repository.SettingsRepository;
import com.pluto.pluto_interview.util.UserIdUtil;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class SettingsService {
	private final SettingsRepository settingsRepository;
	private final StringRedisTemplate stringRedisTemplate;
	private final ApplicationEventPublisher applicationEventPublisher;

	public SettingsService(SettingsRepository settingsRepository, StringRedisTemplate stringRedisTemplate, ApplicationEventPublisher applicationEventPublisher) {
		this.settingsRepository = settingsRepository;
		this.stringRedisTemplate = stringRedisTemplate;
		this.applicationEventPublisher = applicationEventPublisher;
	}

	// TODO Suss out why a 403 is returned
	public Response getSettings() {
		// Fetch settings from database
//		Settings settings = settingsRepository.findById(userId).orElseThrow(() ->
//			  new UserNotFoundException(ErrorMessage.USER_NOT_FOUND.getErrorMessage(),
//				    new Throwable("Non-existing user ID: " + userId))
//		);
//		// Assemble Response and return
//		List<String> interviewTypes = Arrays.stream(QuestionType.values())
//			  .map(QuestionType::getQuestionType)
//			  .toList();
//		List<String> difficultyLevels = Arrays.stream(DifficultyLevel.values())
//			  .map(DifficultyLevel::getDifficultyLevel)
//			  .toList();
//		SettingsResponse settingsResponse = new SettingsResponse(interviewTypes,
////			  Arrays.stream(settings.getPreferredQuestionTypes().split(", ")).toList(),
//			  QuestionTypeMapper.toStringList(settings.getPreferredQuestionTypes()),
//			  difficultyLevels,
//			  settings.getPreferredDifficultyLevel().toString());

		Long userId = UserIdUtil.getUserId();

		// Get settings from cache
		Map<String, String> settings = getSettingsCache(userId);
		String preferredQuestionTypes = settings.get("preferredQuestionTypes");
		List<String> preferredQuestionTypesList = QuestionTypeMapper.toStringList(preferredQuestionTypes);
		String preferredDifficultyLevel = settings.get("preferredDifficultyLevel");

		// Assemble SettingsDetail
		SettingsDetail settingsDetail = new SettingsDetail(
			  Question.QuestionType.getQuestionTypes(),
			  preferredQuestionTypesList,
			  Question.DifficultyLevel.getDifficultyLevels(),
			  preferredDifficultyLevel
		);

		// Return
		return Response.ok(settingsDetail);
	}

//	public Response adjustSettings(@Validated NewSettings newSettings) {
//		// Get user ID, verify settings existence and assemble new settings
//		Long userId = UserIdUtil.getUserId();
//		Settings settings = settingsRepository.findById(userId).orElseThrow(() ->
//			  new UserNotFoundException(ErrorMessage.USER_NOT_FOUND.getErrorMessage()));
//
//		if (newSettings.preferredQuestionTypes() != null) {
//			List<Question.QuestionType> preferredQuestionTypes = verifyAndConvertToPreferredQuestionTypes(newSettings);
//			settings.adjustPreferredQuestionTypes(preferredQuestionTypes);
//		} else {
//			settings.adjustPreferredQuestionTypes(null);
//		}
//
//		if (newSettings.preferredDifficultyLevel() == null) {
//			throw new IllegalDifficultyLevelException(ErrorMessage.ILLEGAL_DIFFICULTY_LEVEL.getErrorMessage());
//		}
//		Question.DifficultyLevel preferredDifficultyLevel = verifyAndConvertToPreferredDifficultyLevel(newSettings);
//		settings.setPreferredDifficultyLevel(preferredDifficultyLevel);
//
//		// Save the new settings and assemble response
//		Settings savedSettings = settingsRepository.save(settings);
//		SettingsResponse settingsResponse = getSettingsResponseFrom(savedSettings);
//
//		log.info("User (ID: {}) updated their settings (Arguments: {}).", userId, newSettings);
//		return Response.ok(settingsResponse);
//	}

	/**
	 * Update settings by publishing a SettingsChangedEvent
	 */
	public Response updateSettings(NewSettings newSettings) {
		Long userId = UserIdUtil.getUserId();
		applicationEventPublisher.publishEvent(new SettingsChangedEvent(userId, newSettings));

		return Response.ok();
	}

	public record SettingsChangedEvent (
		  @NonNull
		  Long userId,

		  @NonNull
		  NewSettings newSettings
	) {}

	private static SettingsResponse getSettingsResponseFrom(Settings savedSettings) {
		return new SettingsResponse(
			  Arrays.stream(Question.QuestionType.values())
				    .map(Enum::toString)
				    .toList(),
//			  savedSettings.getPreferredQuestionTypes().stream()
//				    .map(Enum::toString)
//				    .toList(),
			  QuestionTypeMapper.toStringList(savedSettings.getPreferredQuestionTypes()),
			  Arrays.stream(Question.DifficultyLevel.values())
				    .map(Enum::toString)
				    .toList(),
			  savedSettings.getPreferredDifficultyLevel().toString()
		);
	}

	private static Question.DifficultyLevel verifyAndConvertToPreferredDifficultyLevel(
		  NewSettings newSettings) {
		return Arrays.stream(Question.DifficultyLevel.values())
			  .filter(dl -> newSettings.preferredDifficultyLevel()
				    .equalsIgnoreCase(dl.toString()))
			  .findFirst()
			  .orElseThrow(() ->
				    new IllegalDifficultyLevelException(ErrorMessage
					      .ILLEGAL_DIFFICULTY_LEVEL.getErrorMessage()));
	}

	private static List<Question.QuestionType> verifyAndConvertToPreferredQuestionTypes(NewSettings newSettings) {
		return newSettings.preferredQuestionTypes().stream()
			  .map(pqt -> Arrays.stream(Question.QuestionType.values())
				    .filter(qt ->
						qt.toString().equalsIgnoreCase(pqt))
				    .findFirst()
				    .orElseThrow(() ->
						new IllegalQuestionTypeException(ErrorMessage.ILLEGAL_QUESTION_TYPE.getErrorMessage())))
			  .toList();
	}

	/**
	 * Get settings from cache
	 * @return A {@link Map} containing settings. The keys are {@code preferredQuestionTypes} and
	 * {@code preferredDifficultyLevel}.
	 */
	private Map<String, String> getSettingsCache(Long userId) {
		String key = UserListener.CACHE_KEY_PREFIX + userId;
		String preferredQuestionTypesKey = "preferredQuestionTypes";
		String preferredDifficultyLevelKey = "preferredDifficultyLevel";
		List<Object> hashKeys = Arrays.asList(preferredQuestionTypesKey, preferredDifficultyLevelKey);

		List<Object> hashValues = stringRedisTemplate.opsForHash().multiGet(key, hashKeys);

		return Map.of(preferredQuestionTypesKey, (String) hashValues.get(0),
			  preferredDifficultyLevelKey, (String) hashValues.get(1));
	}
}
