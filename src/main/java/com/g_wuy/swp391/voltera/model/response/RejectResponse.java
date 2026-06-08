package com.g_wuy.swp391.voltera.model.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RejectResponse {
    private Integer postId;
    private String title;
    private String status;
    private String rejectBy;
    private LocalDateTime rejectAt;
    private String reason;

}
