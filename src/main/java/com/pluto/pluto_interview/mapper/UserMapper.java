package com.pluto.pluto_interview.mapper;

import com.pluto.pluto_interview.model.Settings;
import com.pluto.pluto_interview.model.User;
import com.pluto.pluto_interview.model.vo.UserVo;
import org.mapstruct.Mapper;

import java.util.HashMap;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface UserMapper {
	UserVo toVo(User user);

//	default Map<String, String> toRedisHash(User user) {
//		if (user == null) {
//			return null;
//		}
//
//		Map<String, String> map = new HashMap<>();
//		map.put("id", String.valueOf(user.getId()));
//		map.put("email", user.getEmail());
//		map.put("username", user.getUsername());
//
//		Settings settings = user.getSettings();
//		if (settings != null) {
//			String preferredQuestionTypes = settings.getPreferredQuestionTypes();
//			if (preferredQuestionTypes != null && !preferredQuestionTypes.isBlank()) {
//				map.put("preferredQuestionTypes", preferredQuestionTypes);
//			}
//			if (settings.getPreferredDifficultyLevel() != null) {
//				map.put("preferredDifficultyLevel", settings.getPreferredDifficultyLevel().toString());
//			}
//		}
//
//		return map;
//	}
}
