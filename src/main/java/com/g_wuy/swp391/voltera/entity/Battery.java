package com.g_wuy.swp391.voltera.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "battery")
public class Battery {
    @Id
    @Column(name = "post_id", nullable = false)
    private Integer id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "post_id", nullable = false)
    @JsonIgnore
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "battery_type_id")
    private BatteryType batteryTypeId;

    @Size(max = 100)
    @NotNull
    @Column(name = "serial_number", nullable = false, length = 100)
    private String serialNumber;

    @Column(name = "origin_capacity", precision = 10, scale = 2)
    private BigDecimal originCapacity;

    @Column(name = "remaining_capacity", precision = 10, scale = 2)
    private BigDecimal remainingCapacity;

    @Column(name = "mileage_covered")
    private Integer mileageCovered;

    @Column(name = "voltage", precision = 10, scale = 2)
    private BigDecimal voltage;

    @Column(name = "cycle_count")
    private Integer cycleCount;

    @Size(max = 100)
    @Column(name = "warranty", length = 100)
    private String warranty;

    @Column(name = "weight", precision = 10, scale = 2)
    private BigDecimal weight;

    @Size(max = 100)
    @Column(name = "lifecycle", length = 100)
    private String lifecycle;

    @OneToMany(mappedBy = "battery", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BatteryImage> images = new ArrayList<>();


    @Size(max = 20)
    @ColumnDefault("'AVAILABLE'")
    @Column(name = "status", length = 20)
    private String status;

}