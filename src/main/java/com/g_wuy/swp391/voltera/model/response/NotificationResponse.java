package com.g_wuy.swp391.voltera.model.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class NotificationResponse {
    private Integer id;
    private String title;
    private String message;
    private Instant createdAt;
    private boolean readStatus;
}
