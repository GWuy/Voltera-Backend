package com.g_wuy.swp391.voltera.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payment")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id", nullable = false)
    private Integer paymentid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", referencedColumnName = "transaction_id")
    private Transaction transaction;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "payment_status", length = 20)
    private String paymentStatus; // PENDING, SUCCESS, FAILED

    @Column(name = "transaction_code", length = 100)
    private String transactionCode; // vnp_TxnRef

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    // Thêm các field mới cho VNPay
    @Column(name = "vnp_transaction_no", length = 100)
    private String vnpTransactionNo; // Mã giao dịch tại VNPay

    @Column(name = "vnp_bank_code", length = 20)
    private String vnpBankCode; // Mã ngân hàng

    @Column(name = "vnp_bank_tran_no", length = 255)
    private String vnpBankTranNo; // Mã giao dịch tại ngân hàng

    @Column(name = "vnp_card_type", length = 20)
    private String vnpCardType; // Loại thẻ: ATM, QRCODE

    @Column(name = "vnp_pay_date", length = 14)
    private String vnpPayDate; // Thời gian thanh toán

    @Column(name = "vnp_response_code", length = 2)
    private String vnpResponseCode; // Mã phản hồi

    @Column(name = "amount", precision = 12, scale = 2)
    private BigDecimal amount; // Số tiền

    @Column(name = "order_info", length = 255)
    private String orderInfo; // Thông tin đơn hàng

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}