package com.g_wuy.swp391.voltera.model.request;

import lombok.Data;

@Data
public class ProfileRequest {
    private String firstname;
    private String lastname;
    private String email;
    private String phone;
    private Boolean gender;
    private String address;
}
