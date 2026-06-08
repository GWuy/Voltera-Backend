package com.g_wuy.swp391.voltera.mapper;

import com.g_wuy.swp391.voltera.entity.Contract;
import com.g_wuy.swp391.voltera.entity.Post;
import com.g_wuy.swp391.voltera.entity.User;
import com.g_wuy.swp391.voltera.model.request.ContractRequest;
import com.g_wuy.swp391.voltera.model.response.ContractResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ContractMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "postid", source = "post")
    @Mapping(target = "buyerid", source = "buyer")
    @Mapping(target = "sellerid", source = "seller")
    @Mapping(target = "contractstatus", constant = "PENDING")
    @Mapping(target = "buyersigned", constant = "false")
    @Mapping(target = "sellersigned", constant = "false")
    Contract toEntity(ContractRequest request, Post post, User buyer, User seller);

    @Mapping(source = "id", target = "contractId")
    @Mapping(source = "postid.id", target = "postId")
    @Mapping(source = "postid.title", target = "postTitle")
    @Mapping(source = "buyerid.fullname", target = "buyerName")
    @Mapping(source = "buyerid.email", target = "buyerEmail")
    @Mapping(source = "sellerid.fullname", target = "sellerName")
    @Mapping(source = "sellerid.email", target = "sellerEmail")
    @Mapping(source = "buyersigned", target = "signedByBuyer")
    @Mapping(source = "sellersigned", target = "signedBySeller")
    @Mapping(source = "contractstatus", target = "contractStatus")
    @Mapping(source = "signeddate", target = "signedDate")
    @Mapping(source = "contractfile", target = "contractFile")
    @Mapping(target = "transactionStatus", ignore = true)
    ContractResponse toResponse(Contract contract);

}
