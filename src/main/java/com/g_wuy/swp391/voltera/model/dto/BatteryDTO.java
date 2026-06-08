package com.g_wuy.swp391.voltera.model.dto;

import java.math.BigDecimal;

import com.g_wuy.swp391.voltera.model.dto.BatteryTypeDTO;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BatteryDTO {
    @NotBlank(message = "Serial number is required")
    private String serialNumber;
    @NotNull(message = "Origin capacity is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Origin capacity must be greater than 0")
    private BigDecimal originCapacity;
    @NotNull(message = "Remaining capacity is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Remaining capacity cannot be negative")
    private BigDecimal remainingCapacity;
    @NotNull(message = "Mileage covered is required")
    @Min(value = 0, message = "Mileage cannot be negative")
    private Integer mileageCovered;
    @NotNull(message = "Voltage is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Voltage must be greater than 0")
    private BigDecimal voltage;
    @NotNull(message = "Cycle count is required")
    @Min(value = 0, message = "Cycle count cannot be negative")
    private Integer cycleCount;
    private String warranty;
    private BigDecimal weight;
    private String lifecycle;
    private BatteryTypeDTO batteryTypeId;
}
