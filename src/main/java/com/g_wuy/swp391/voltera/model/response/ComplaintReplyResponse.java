package com.g_wuy.swp391.voltera.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ComplaintReplyResponse {
    private Integer id;
    private String problem;
    private String solution;
    private String resolveAt;
}
