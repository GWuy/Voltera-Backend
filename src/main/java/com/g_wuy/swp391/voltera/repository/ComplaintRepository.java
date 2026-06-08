package com.g_wuy.swp391.voltera.repository;

import com.g_wuy.swp391.voltera.entity.Complaint;
import com.g_wuy.swp391.voltera.entity.ComplaintReply;
import com.g_wuy.swp391.voltera.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Integer> {
    List<Complaint> getComplaintsByStatusIgnoreCase(String status);

    @Query("SELECT c FROM Complaint c WHERE c.senderId.id = :id")
    List<Complaint> viewComplaintByUserId(Integer id);

    List<Complaint> findByResolveAtIsNull();

    @Query("SELECT c FROM Complaint c WHERE c.senderId = :senderId")
    Optional<Complaint> findBySenderId(Integer senderId);

    @Query("SELECT c FROM Complaint c WHERE c.id = :complaintId")
    Complaint findComplaintById(@Param("complaintId") Integer complaintId);
}