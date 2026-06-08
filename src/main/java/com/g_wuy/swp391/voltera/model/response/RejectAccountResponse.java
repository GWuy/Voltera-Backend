package com.g_wuy.swp391.voltera.model.response;

import lombok.Data;

import java.time.Instant;

@Data
public class RejectAccountResponse {
    private Integer accountId;      // ID của account được approve
    private String username;        // Username của account
    private String status;          // Trạng thái hiện tại, ví dụ: "approved"
    private Instant rejectAt;     // Thời gian approve
    private Integer userId;
}
