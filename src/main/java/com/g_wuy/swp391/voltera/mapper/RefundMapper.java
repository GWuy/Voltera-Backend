package com.g_wuy.swp391.voltera.mapper;

import com.g_wuy.swp391.voltera.entity.Refund;
import com.g_wuy.swp391.voltera.model.response.RefundResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RefundMapper {
    @Mapping(source = "sender.fullname", target = "senderName")
    @Mapping(source = "receiver.fullname", target = "receiverName")
    @Mapping(source = "transaction.post.title", target = "title")
    @Mapping(source = "amount", target = "amount")
    @Mapping(source = "reason", target = "reason")
    @Mapping(source = "refundStatus", target = "refundStatus")
    @Mapping(source = "createdAt", target = "createdAt")
    RefundResponse toRefundResponse(Refund refund);
}