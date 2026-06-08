package com.g_wuy.swp391.voltera.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReplyComplaintRequest {
    @NotBlank(message = "message is required")
    private String message;
}
