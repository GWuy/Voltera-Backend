package com.g_wuy.swp391.voltera.mapper;

import com.g_wuy.swp391.voltera.model.response.ApproveResponse;
import com.g_wuy.swp391.voltera.model.response.LoginResponse;
import com.g_wuy.swp391.voltera.model.response.RegisterResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.g_wuy.swp391.voltera.entity.Account;
import com.g_wuy.swp391.voltera.model.request.RegisterRequest;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    @Mapping(source = "id", target = "userId")
    @Mapping(source = "role", target = "role")
    LoginResponse toLoginResponse(Account account);

    Account toAccount(RegisterRequest request);

    RegisterResponse toRegisterResponse(Account account);

    @Mapping(source = "id", target = "accountId")
    @Mapping(source = "id", target = "userId")
    @Mapping(source = "role", target = "role")
    @Mapping(source = "user.email", target = "email")
    @Mapping(source = "user.fullname", target = "fullname")
    @Mapping(source = "createat", target = "createdAt")
    @Mapping(target = "approvedAt", ignore = true)
    ApproveResponse toAccountResponse(Account account);
}
