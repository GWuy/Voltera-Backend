package com.g_wuy.swp391.voltera.model.response;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class FavListResponse {
    private Integer userId;
    private Integer postId;
    private String postTitle;
    private BigDecimal price;
    private String thumbnailUrl;
}