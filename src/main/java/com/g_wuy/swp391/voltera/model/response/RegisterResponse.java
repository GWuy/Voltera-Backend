package com.g_wuy.swp391.voltera.model.response;

import lombok.Data;

@Data
public class RegisterResponse {
    private Integer id;
    private String email;
    private String username;
    private String role;
    private String status;
}
