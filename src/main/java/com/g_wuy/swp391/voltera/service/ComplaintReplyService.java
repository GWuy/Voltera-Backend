package com.g_wuy.swp391.voltera.service;

import com.g_wuy.swp391.voltera.entity.Complaint;
import com.g_wuy.swp391.voltera.entity.ComplaintReply;
import com.g_wuy.swp391.voltera.entity.User;
import com.g_wuy.swp391.voltera.exception.BusinessException;
import com.g_wuy.swp391.voltera.mapper.ComplaintReplyMapper;
import com.g_wuy.swp391.voltera.model.request.ReplyComplaintRequest;
import com.g_wuy.swp391.voltera.model.response.ComplaintReplyResponse;
import com.g_wuy.swp391.voltera.model.response.ComplaintResponse;
import com.g_wuy.swp391.voltera.repository.ComplaintReplyRepository;
import com.g_wuy.swp391.voltera.repository.ComplaintRepository;
import com.g_wuy.swp391.voltera.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestHeader;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class ComplaintReplyService {

    ComplaintReplyRepository complaintReplyRepository;

    JwtService jwtService;

    ComplaintRepository complaintRepository;

    UserRepository userRepository;

    ComplaintReplyMapper complaintReplyMapper;

    public ResponseEntity<ComplaintReply> replyComplaint(
            ReplyComplaintRequest request,
            Integer complaintId,
            @RequestHeader("Authorization") String token) {

        String jwt = token.substring(7);
        User sender = userRepository.findUserByUsername(jwtService.extractUsername(jwt));

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));

        User receiver = complaint.getSenderId();

        ComplaintReply reply = new ComplaintReply();
        reply.setComplaintId(complaint);
        reply.setSenderId(sender);
        reply.setReceiverId(receiver);
        reply.setMessage(request.getMessage());
        reply.setCreatedAt(Instant.now());
        reply.setResolveAt(Instant.now());

        ComplaintReply savedReply = complaintReplyRepository.save(reply);

        return ResponseEntity.ok(savedReply);
    }

    public ResponseEntity<List<ComplaintReplyResponse>> myReplyComplaint(@RequestHeader("Authorization") String token) {
        String jwt = token.substring(7);
        User receiver = userRepository.findUserByUsername(jwtService.extractUsername(jwt));
        if (receiver == null) {
            throw new BusinessException("User not found");
        }
        List<ComplaintReply> complaintReplies = complaintReplyRepository.findComplaintRepliesByReceiverId(receiver.getId());
//        List<Complaint> complaints = complaintRepository.viewComplaintByUserId(receiver.getId());
        List<ComplaintReplyResponse> responses = new ArrayList<>();
        for (ComplaintReply reply : complaintReplies) {
            Complaint complaint = reply.getComplaintId();
            responses.add(complaintReplyMapper.toComplaintReplyResponse(complaint, reply));
        }
        return ResponseEntity.ok(responses);
    }

    public ResponseEntity<List<ComplaintResponse>> listComplaintNotResolve() {
        List<Complaint> unresolved = complaintRepository.findByResolveAtIsNull();

        List<ComplaintResponse> responses = unresolved.stream()
                .map(c -> new ComplaintResponse(
                        c.getId(),
                        c.getSenderId().getFullname(),
                        c.getProblem(),
                        c.getDescription(),
                        c.getCreateAt()
                ))
                .toList();

        return ResponseEntity.ok(responses);
    }

    public ResponseEntity<List<ComplaintResponse>> searchComplaintByProblem(String problem) {
        return ResponseEntity.ok(complaintReplyRepository.findComplaintRepliesByProblem(problem));
    }
}
