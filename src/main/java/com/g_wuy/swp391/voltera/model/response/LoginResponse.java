package com.g_wuy.swp391.voltera.model.response;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class LoginResponse {
    private Integer userId;
    private String token;
    private String role;
    private boolean isUpdatedProfile;

}
