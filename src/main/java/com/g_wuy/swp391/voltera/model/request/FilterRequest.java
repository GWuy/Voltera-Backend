package com.g_wuy.swp391.voltera.model.request;

import jakarta.annotation.Nullable;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FilterRequest {
    private String keyword;
    @Nullable
    private String address;
    @Nullable
    private String brand;
    @Nullable
    private String version;
    @Nullable
    private Integer minOdo;
    @Nullable
    private Integer maxOdo;
    @Nullable
    private Integer minRange;
    @Nullable
    private Integer maxRange;
    @Nullable
    private String color;
    @Nullable
    private String origin;
    @Nullable
    private String style;
    @Nullable
    private Boolean bodyInsurance;
    @Nullable
    private Boolean vehicleInspection;
    @Nullable
    private BigDecimal minPrice;
    @Nullable
    private BigDecimal maxPrice;
    @Nullable
    private Integer minYearManufacture;
    @Nullable
    private Integer maxYearManufacture;
    @Nullable
    private Integer numberOfSeat;
}