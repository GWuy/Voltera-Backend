package com.g_wuy.swp391.voltera.mapper;

import com.g_wuy.swp391.voltera.entity.Transaction;
import com.g_wuy.swp391.voltera.model.response.TransactionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(source = "transactionid", target = "id")
    @Mapping(source = "contractid.postid.id", target = "postId")
    @Mapping(source = "contractid.postid.title", target = "postTitle")
    @Mapping(source = "price", target = "price")
    @Mapping(source = "transactionStatus", target = "transactionStatus")
    @Mapping(source = "createAt", target = "createAt")
    @Mapping(source = "updateAt", target = "updateAt")
    TransactionResponse toResponse(Transaction transaction);
}
