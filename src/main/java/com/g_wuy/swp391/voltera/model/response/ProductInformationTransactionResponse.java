package com.g_wuy.swp391.voltera.model.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@Data
public class ProductInformationTransactionResponse {
    String type;
    String title;
    String brand;
    String model;
    String version;
    Integer odo;
    BigDecimal batteryCapacity;
    int yearManufacture;
    String color;
    BigDecimal price;

}
