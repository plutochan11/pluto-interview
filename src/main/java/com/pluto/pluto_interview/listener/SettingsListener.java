package com.pluto.pluto_interview.listener;

import com.pluto.pluto_interview.model.Settings;
import com.pluto.pluto_interview.model.dto.NewSettings;
import com.pluto.pluto_interview.repository.SettingsRepository;
import com.pluto.pluto_interview.service.SettingsService;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Component
public class SettingsListener {
	private final StringRedisTemplate stringRedisTemplate;
	private final SettingsRepository settingsRepository;

	public SettingsListener(StringRedisTemplate stringRedisTemplate, SettingsRepository settingsRepository) {
		this.stringRedisTemplate = stringRedisTemplate;
		this.settingsRepository = settingsRepository;
	}

	// TODO Complete this method
	/**
	 * Handles settings update events.
	 * @throws NoSuchElementException if settings for the given user ID do not exist in the database.
	 */
	@Async
	@EventListener(SettingsService.SettingsChangedEvent.class)
	public void updateSettings(SettingsService.SettingsChangedEvent event) {
		Long userId = event.userId();

		// Verify settings' existence in database
		Settings settings = settingsRepository.findById(userId)
			  .orElseThrow(() -> new NoSuchElementException("Settings not found for user ID: " + userId));

		// Update settings in cache
		String key = UserListener.CACHE_KEY_PREFIX + userId;
		NewSettings newSettings = event.newSettings();
		String questionTypes = String.join(", ", newSettings.preferredQuestionTypes());
		String difficultyLevel = newSettings.preferredDifficultyLevel();
		Map<String, String> hashValues = Map.of(
			  "preferredQuestionTypes", questionTypes,
			  "preferredDifficultyLevel", difficultyLevel
		);

		stringRedisTemplate.opsForHash().putAll(key, hashValues);

		// Update settings in database
//		settings.setPreferredQuestionTypes();
	}
}
