package com.g_wuy.swp391.voltera.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "battery_type")
public class BatteryType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 100)
    @NotNull
    @Column(name = "type_name", nullable = false, length = 100)
    private String typename;

    @Size(max = 200)
    @Column(name = "technical", length = 200)
    private String technical;

    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;

}