package com.g_wuy.swp391.voltera.repository;

import com.g_wuy.swp391.voltera.entity.Fee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FeeRepository extends JpaRepository<Fee, Integer> {

    @Query("SELECT f FROM Fee f WHERE f.post.id = :postId AND (f.feeStatus = 'PENDING' OR f.feeStatus = 'PAID') ORDER BY f.createdAt DESC")
    Optional<Fee> findValidFeeByPostId(@Param("postId") Integer postId);

    List<Fee> findByCreatedAtBeforeAndFeeStatus(LocalDateTime dateTime, String status);

    @Query("SELECT f FROM Fee f WHERE f.transaction.transactionid = :transactionId")
    Fee findByTransactionId(@Param("transactionId") Integer transactionId);

    @Query("SELECT f FROM Fee f WHERE f.post.id = :postId ORDER BY f.createdAt DESC")
    List<Fee> findFeesByPostIdOrderByCreatedAtDesc(@Param("postId") Integer postId);
}