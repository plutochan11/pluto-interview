package com.pluto.pluto_interview.mapper;

import com.pluto.pluto_interview.model.User;
import com.pluto.pluto_interview.model.vo.UserVo;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
	public default UserVo toVo(User user) {
		if (user == null) {
			return null;
		}
		return UserVo.builder()
			  .id(user.getId())
			  .email(user.getEmail())
			  .username(user.getUsername())
			  .build();
	}
}
