package com.g_wuy.swp391.voltera.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundResponse {
    private Integer id;
    private String senderName;
    private String receiverName;
    private BigDecimal amount;
    private String reason;
    private String title;
    private String refundStatus;
    private Instant createdAt;
}