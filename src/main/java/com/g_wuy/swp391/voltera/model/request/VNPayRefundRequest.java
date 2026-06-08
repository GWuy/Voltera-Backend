package com.g_wuy.swp391.voltera.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VNPayRefundRequest {
    private String transactionType;
    private String orderId;
    private Long amount;
    private String transactionDate;
    private String createdBy;
}
