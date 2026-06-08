package com.g_wuy.swp391.voltera.model.response;

import lombok.Data;

import java.time.Instant;

@Data
public class ProfileResponse {
    private Integer id;
    private String firstname;
    private String lastname;
    private String fullname;
    private String email;
    private String phone;
    private Boolean gender;
    private String address;
    private Instant createAt;
    private Instant updateAt;
    private String avatar;
}
