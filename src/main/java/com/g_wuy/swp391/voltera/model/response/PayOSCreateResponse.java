package com.g_wuy.swp391.voltera.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayOSCreateResponse {
    private String checkoutUrl;
    private String paymentLinkId;
    private Long orderCode;
    private Integer transactionId;
}
