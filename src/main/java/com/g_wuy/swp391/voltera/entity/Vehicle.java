package com.g_wuy.swp391.voltera.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "vehicle")
public class Vehicle {
    @Id
    @Column(name = "post_id", nullable = false)
    private Integer id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "post_id", nullable = false)
    @JsonIgnore
    private Post post;


    @Size(max = 100)
    @Column(name = "brand", length = 100)
    private String brand;

    @Size(max = 100)
    @Column(name = "model", length = 100)
    private String model;

    @Size(max = 50)
    @Column(name = "version", length = 50)
    private String version;

    @Column(name = "odo")
    private Integer odo;

    @Column(name = "battery_capacity", precision = 10, scale = 2)
    private BigDecimal batteryCapacity;

    @Column(name = "range")
    private Integer range;

    @Column(name = "charging_time")
    private Integer chargingTime;

    @Size(max = 50)
    @Column(name = "color", length = 50)
    private String color;

    @Column(name = "number_of_seat")
    private Integer numberOfSeat;

    @Size(max = 50)
    @Column(name = "style", length = 50)
    private String style;

    @ColumnDefault("false")
    @Column(name = "body_insurance")
    private Boolean bodyInsurance;

    @ColumnDefault("false")
    @Column(name = "vehicle_inspection")
    private Boolean vehicleInspection;

    @Size(max = 8)
    @Column(name = "license_plate", length = 8)
    private String licensePlate;

    @Size(max = 255)
    @Column(name = "origin")
    private String origin;

    @Size(max = 20)
    @Column(name = "status", length = 20)
    private String status;

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<VehicleImage> images = new ArrayList<>();


    @Column(name = "year_manufacture")
    private int yearManufacture;
}