package com.g_wuy.swp391.voltera.repository;

import com.g_wuy.swp391.voltera.entity.Refund;
import com.g_wuy.swp391.voltera.model.response.RefundResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Integer> {


    @Query("SELECT " +
            "new com.g_wuy.swp391.voltera.model.response.RefundResponse(r.id, r.sender.fullname, r.receiver.fullname, r.transaction.price, r.reason, r.transaction.post.title, r.refundStatus, r.createdAt) " +
            "FROM Refund r " +
            "WHERE r.sender.id = :senderId AND r.refundStatus = :status")
    List<RefundResponse> getRefundBySenderIdAndStatus(@Param("senderId") Integer senderId, @Param("status") String status);


    @Query("SELECT " +
            "new com.g_wuy.swp391.voltera.model.response.RefundResponse(r.id, r.sender.fullname, r.receiver.fullname, r.transaction.price, r.reason, r.transaction.post.title, r.refundStatus, r.createdAt) " +
            "FROM Refund r " +
            "WHERE r.sender.id = :senderId")
    List<RefundResponse> getAllRefundBySenderId(@Param("senderId") Integer senderId);

    @Query("SELECT " +
            "new com.g_wuy.swp391.voltera.model.response.RefundResponse(r.id, r.sender.fullname, r.receiver.fullname, r.transaction.price, r.reason, r.transaction.post.title, r.refundStatus, r.createdAt) " +
            "FROM Refund r " +
            "WHERE r.receiver.id = :receiverId AND r.refundStatus = :status")
    List<RefundResponse> getRefundByReceiverIdAndStatus(@Param("receiverId") Integer receiverId, @Param("status") String status);

    @Query("SELECT " +
            "new com.g_wuy.swp391.voltera.model.response.RefundResponse(r.id, r.sender.fullname, r.receiver.fullname, r.transaction.price, r.reason, r.transaction.post.title, r.refundStatus, r.createdAt) " +
            "FROM Refund r " +
            "WHERE r.receiver.id = :receiverId")
    List<RefundResponse> getRefundByReceiverId(@Param("receiverId") Integer receiverId);


    Refund findRefundById(Integer refundId);

    // Admin queries
    @Query("SELECT r FROM Refund r")
    List<Refund> findAllBuyerRefunds();

    @Query("SELECT r FROM Refund r WHERE r.refundStatus = :status")
    List<Refund> findAllBuyerRefundsByStatus(@Param("status") String status);

    @Query("SELECT r FROM Refund r")
    List<Refund> findAllSellerRefunds();

    @Query("SELECT r FROM Refund r WHERE r.refundStatus = :status")
    List<Refund> findAllSellerRefundsByStatus(@Param("status") String status);

    @Query("SELECT r FROM Refund r WHERE r.refundStatus = :status")
    List<Refund> findByRefundStatus(@Param("status") String status);

    @Query("SELECT r FROM Refund r WHERE r.transaction = :transaction")
    List<Refund> findByTransaction(@Param("transaction") com.g_wuy.swp391.voltera.entity.Transaction transaction);
}