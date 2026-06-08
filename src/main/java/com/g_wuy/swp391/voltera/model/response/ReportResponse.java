package com.g_wuy.swp391.voltera.model.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class ReportResponse {
    private Integer id;
    private Integer month;
    private Integer year;
    private BigDecimal totalRevenue;
    private Long totalTransactions;
    private Instant createdAt;
}
