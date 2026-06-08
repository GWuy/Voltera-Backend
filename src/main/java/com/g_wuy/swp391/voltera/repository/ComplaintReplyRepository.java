package com.g_wuy.swp391.voltera.repository;

import com.g_wuy.swp391.voltera.entity.Complaint;
import com.g_wuy.swp391.voltera.entity.ComplaintReply;
import com.g_wuy.swp391.voltera.entity.User;
import com.g_wuy.swp391.voltera.model.response.ComplaintResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComplaintReplyRepository extends JpaRepository<ComplaintReply, Integer> {
    @Query("SELECT c.senderId FROM Complaint c WHERE c.id = :complaintId")
    User getReceiverIdByComplaintId(@Param("complaintId") Integer complaintId);

    List<ComplaintReply> findComplaintRepliesByResolveAtIsNull();

    @Query("SELECT cr FROM ComplaintReply cr WHERE cr.receiverId.id = :receiverId")
    List<ComplaintReply> findComplaintRepliesByReceiverId(Integer receiverId);

    @Query("SELECT new com.g_wuy.swp391.voltera.model.response.ComplaintResponse(c.id, c.senderId.fullname, c.problem, c.description, c.createAt) " +
            "FROM Complaint c WHERE c.problem LIKE %:problem%")
    List<ComplaintResponse> findComplaintRepliesByProblem(@Param("problem") String problem);
}