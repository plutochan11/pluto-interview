package com.pluto.pluto_interview.mapper;

import com.pluto.pluto_interview.model.User;
import com.pluto.pluto_interview.model.vo.UserVo;
import org.mapstruct.Mapper;

import java.util.HashMap;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface UserMapper {
	default UserVo toVo(User user) {
		if (user == null) {
			return null;
		}
		return UserVo.builder()
			  .id(user.getId())
			  .email(user.getEmail())
			  .username(user.getUsername())
			  .build();
	}

	default Map<String, String> toMap(User user) {
		if (user == null) {
			return null;
		}

		Map<String, String> map = new HashMap<>();
		map.put("id", String.valueOf(user.getId()));
		map.put("email", user.getEmail());
		map.put("username", user.getUsername());

		return map;
	}
}
