package com.g_wuy.swp391.voltera.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    private String username;
    @NotBlank(message = "Password is required")
    private String password;
}
