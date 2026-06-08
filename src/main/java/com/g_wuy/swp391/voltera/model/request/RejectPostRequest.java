package com.g_wuy.swp391.voltera.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RejectPostRequest {
    @NotBlank(message = "Reason is required")
    private String reason;
}
