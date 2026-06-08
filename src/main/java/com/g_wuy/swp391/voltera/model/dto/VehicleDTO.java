package com.g_wuy.swp391.voltera.model.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VehicleDTO {
    @NotBlank(message = "Brand is required")
    private String brand;
    @NotBlank(message = "Model is required")
    private String model;
    private String version;
    @NotNull(message = "Odo is required")
    @Min(value = 0, message = "Odo cannot be negative")
    private Integer odo;

    @NotNull(message = "Battery capacity is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Battery capacity must be greater than 0")
    private BigDecimal batteryCapacity;

    @NotNull(message = "Range is required")
    @Min(value = 1, message = "Range must be greater than 0")
    private Integer range;
    private Integer chargingTime;
    @NotBlank(message = "Color is required")
    private String color;
    @NotNull(message = "Number of seat is required")
    @Min(value = 4, message = "Must have at least 4 seat")
    private Integer numberOfSeat;
    private String style;
    @NotNull(message = "Body insurance info is required")
    private Boolean bodyInsurance;
    private Boolean vehicleInspection;
    @NotBlank(message = "License plate is required")
    private String licensePlate;
    @NotBlank(message = "Origin is required")
    private String origin;
    @Min(value = 2000, message = "Year manufacture must be >= 2000")
    private int yearManufacture;
}
