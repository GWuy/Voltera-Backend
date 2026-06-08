package com.g_wuy.swp391.voltera.mapper;

import com.g_wuy.swp391.voltera.entity.Bank;
import com.g_wuy.swp391.voltera.model.response.BankResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BankMapper {

    @Mapping(target = "accountName", source = "accountName")
    @Mapping(target = "bankName", source = "bankName")
    @Mapping(target = "bankNumber", source = "bankNumber")
    @Mapping(target = "balance", source = "balance")
    @Mapping(target = "securityCode", source = "securityCode")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "expDate", source = "expDate")
    BankResponse toBankResponse(Bank bank);
}