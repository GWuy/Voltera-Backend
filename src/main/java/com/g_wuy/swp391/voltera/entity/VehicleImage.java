package com.g_wuy.swp391.voltera.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "vehicle_image")
public class VehicleImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "post_id")
    @JsonBackReference
    private Vehicle vehicle;

    @NotNull
    @Column(name = "image_url", nullable = false, length = Integer.MAX_VALUE)
    private String imageUrl;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "uploaded_at")
    private Instant uploadedAt;

}