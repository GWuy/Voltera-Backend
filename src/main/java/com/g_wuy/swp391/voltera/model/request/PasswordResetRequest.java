package com.g_wuy.swp391.voltera.model.request;

import lombok.Data;

@Data
public class PasswordResetRequest {
    private String email;
    private String otp;
    private String newPassword;
}
