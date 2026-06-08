package com.g_wuy.swp391.voltera.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "refund_image")
public class RefundImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "refund_id", nullable = false)
    private Refund refund;

    @NotNull
    @Column(name = "image_url", nullable = false, length = Integer.MAX_VALUE)
    private String imageUrl;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "uploaded_at")
    private Instant uploadedAt;

}