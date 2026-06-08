package com.g_wuy.swp391.voltera.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bank")
public class Bank {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bank_id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Size(max = 150)
    @NotNull
    @Column(name = "bank_name", nullable = false, length = 150)
    private String bankName;

    @Size(max = 50)
    @NotNull
    @Column(name = "account_number", nullable = false, length = 50)
    private String bankNumber;

    @Size(max = 200)
    @NotNull
    @Column(name = "account_name", nullable = false, length = 200)
    private String accountName;

    @Column(name = "security_code")
    private Integer securityCode;

    @Column(name = "exp_date")
    private LocalDate expDate;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Size(max = 20)
    @Column(name = "status", length = 20)
    private String status;

    @ColumnDefault("0")
    @Column(name = "balance", precision = 12, scale = 2)
    private BigDecimal balance;

    @PrePersist
    public void onCreate() {
        this.createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}