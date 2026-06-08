package com.g_wuy.swp391.voltera.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "review")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id")
    private User writerId;

    @Column(name = "content", length = Integer.MAX_VALUE)
    private String content;

    @Column(name = "rating")
    private Integer rating;

}