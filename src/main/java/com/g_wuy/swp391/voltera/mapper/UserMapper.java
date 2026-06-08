package com.g_wuy.swp391.voltera.mapper;

import com.g_wuy.swp391.voltera.model.response.ProfileResponse;
import org.mapstruct.Mapper;

import com.g_wuy.swp391.voltera.entity.Account;
import com.g_wuy.swp391.voltera.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(Account account);

    ProfileResponse toProfileResponse(User user);
}