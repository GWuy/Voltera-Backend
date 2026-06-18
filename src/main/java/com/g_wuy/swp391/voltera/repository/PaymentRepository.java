package com.g_wuy.swp391.voltera.repository;

import com.g_wuy.swp391.voltera.entity.Payment;
import com.g_wuy.swp391.voltera.model.response.PaymentPrepareResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    Optional<Payment> findByTransactionCode(String transactionCode);

    Optional<Payment> findByVnpTransactionNo(String vnpTransactionNo);

    boolean existsByTransactionCode(String transactionCode);

    @Query("SELECT new com.g_wuy.swp391.voltera.model.response.PaymentPrepareResponse(po.id, pa.amount, pa.orderInfo) " +
            "FROM Payment pa JOIN Transaction t ON pa.transaction.transactionid = t.transactionid " +
            "JOIN Post po ON po.id = t.post.id " +
            "WHERE t.transactionid = :transactionId AND po.sellerId.id = :sellerId")
    ResponseEntity<PaymentPrepareResponse> findPaymentByTransactionId(@Param("transactionId") Integer transactionId, @Param("sellerId") Integer sellerId);

    @Query("SELECT pa FROM Payment pa WHERE pa.transaction.transactionid = :transactionId")
    Payment findPaymentByTransactionId(@Param("transactionId") Integer transactionId);

}