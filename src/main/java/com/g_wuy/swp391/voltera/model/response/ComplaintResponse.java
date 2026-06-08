package com.g_wuy.swp391.voltera.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ComplaintResponse {
    private Integer complaintId;
    private String senderFullName;
    private String problem;
    private String description;
    private LocalDateTime createAt;
}