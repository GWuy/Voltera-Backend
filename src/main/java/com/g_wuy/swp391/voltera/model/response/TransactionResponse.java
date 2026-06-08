package com.g_wuy.swp391.voltera.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionResponse {
    private Integer id;
    private Integer postId;
    private String postTitle;
    private BigDecimal price;
    private String transactionStatus;
    private Instant createAt;
    private Instant updateAt;
}