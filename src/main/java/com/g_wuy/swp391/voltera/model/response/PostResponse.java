package com.g_wuy.swp391.voltera.model.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.g_wuy.swp391.voltera.model.dto.BatteryDTO;
import com.g_wuy.swp391.voltera.model.dto.VehicleDTO;

import lombok.Data;

@Data
public class PostResponse {
    private Integer postId;
    private String title;
    private String description;
    private BigDecimal price;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
    private BatteryDTO battery;
    private VehicleDTO vehicle;
    private List<String> imageUrls;
    private String location;
    private String thumbnail;
    private String feeStatus;
}