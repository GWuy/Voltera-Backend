package com.g_wuy.swp391.voltera.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VNPayResponse {
    private String code;
    private String message;
    private String paymentUrl;
}
