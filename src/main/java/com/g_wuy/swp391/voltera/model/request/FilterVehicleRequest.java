package com.g_wuy.swp391.voltera.model.request;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class FilterVehicleRequest {
    private String brand;
    private String model;
    private String color;
    private String origin;
    private String style;
    private boolean bodyInsurance;
    private boolean vehicleInspection;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private int yearManufacture;
}