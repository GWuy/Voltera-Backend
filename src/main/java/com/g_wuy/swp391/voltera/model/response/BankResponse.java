package com.g_wuy.swp391.voltera.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BankResponse {
    private String accountName;
    private String bankName;
    private String bankNumber;
    private BigDecimal balance;
    private Integer securityCode;
    private String status;
    private LocalDate expDate;
}