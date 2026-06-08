package com.g_wuy.swp391.voltera.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VNPayRequest {
    private Long amount;
    private Integer postId;
    private String orderInfo;
    private String orderType;
    private String bankCode;
    private String language;
}
