package com.g_wuy.swp391.voltera.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayOSRequest {
    private double amount;
    private String orderInfo;
}
