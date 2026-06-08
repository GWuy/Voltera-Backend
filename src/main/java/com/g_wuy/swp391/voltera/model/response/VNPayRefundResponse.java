package com.g_wuy.swp391.voltera.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VNPayRefundResponse {
    private String code;
    private String message;
    private String responseId;
    private String txnRef;
    private String amount;
    private String bankCode;
    private String transactionNo;
    private String transactionStatus;
}
