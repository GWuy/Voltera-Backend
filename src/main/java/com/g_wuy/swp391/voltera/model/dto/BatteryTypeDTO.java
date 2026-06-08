package com.g_wuy.swp391.voltera.model.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatteryTypeDTO {
    private Integer id;
    private String typeName;
    private String technical;
    private String description;
}