package com.g_wuy.swp391.voltera.model.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class BankRequest {
    private String bankName;
    private String bankNumber;
    private String accountName;
    private int securityCode;
    private LocalDate expirationDate;
}