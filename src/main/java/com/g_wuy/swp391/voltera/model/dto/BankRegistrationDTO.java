package com.g_wuy.swp391.voltera.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BankRegistrationDTO {

    @NotBlank(message = "Bank name is required")
    @Size(max = 150, message = "Bank name must not exceed 150 characters")
    private String bankName;

    @NotBlank(message = "Bank number is required")
    @Pattern(regexp = "\\d{9,16}", message = "Bank number must be 9-16 digits")
    @Size(max = 50, message = "Bank number must not exceed 50 characters")
    private String bankNumber;

    @NotBlank(message = "Account name is required")
    @Size(max = 200, message = "Account name must not exceed 200 characters")
    private String accountName;

    @NotNull(message = "Security code is required")
    @Pattern(regexp = "\\d{3}", message = "Security code must be exactly 3 digits")
    private String securityCode;

    @NotNull(message = "Expiration date is required")
    private LocalDate expDate;

    // Custom validation method for expiration date
    public boolean isExpDateValid() {
        return expDate != null && expDate.isAfter(LocalDate.now());
    }
}