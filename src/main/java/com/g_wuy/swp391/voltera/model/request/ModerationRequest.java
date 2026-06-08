package com.g_wuy.swp391.voltera.model.request;

import lombok.Data;

@Data
public class ModerationRequest {
    private Integer postId;
    private String status;
    private String reason;
}
