package com.g_wuy.swp391.voltera.mapper;

import com.g_wuy.swp391.voltera.entity.Payment;
import com.g_wuy.swp391.voltera.model.response.PaymentPrepareResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentPrepareResponseMapper {

    @Mapping(source = "transaction.post.title", target = "title")
    @Mapping(source = "transaction.price", target = "price")
    PaymentPrepareResponse toPaymentPrepareResponse(Payment payment);
}
