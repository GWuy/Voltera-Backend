package com.g_wuy.swp391.voltera.model.response;

import lombok.Data;

import java.time.Instant;

@Data
public class ApproveResponse {
    private Integer accountId;      // ID của account được approve
    private String username;        // Username của account
    private String status;          // Trạng thái hiện tại, ví dụ: "approved"
    private String role;            // Role của account: ADMIN, SELLER, USER
    private String email;           // Email của account
    private Instant approvedAt;     // Thời gian approve
    private Integer userId;         // ID của User profile liên kết (nếu có)
    private String fullname;
    private Instant createdAt;      // Thời gian tạo account
}