package com.g_wuy.swp391.voltera.service;

import com.g_wuy.swp391.voltera.entity.Account;
import com.g_wuy.swp391.voltera.entity.Complaint;
import com.g_wuy.swp391.voltera.entity.User;
import com.g_wuy.swp391.voltera.exception.BusinessException;
import com.g_wuy.swp391.voltera.mapper.ComplaintMapper;
import com.g_wuy.swp391.voltera.model.request.ComplaintRequest;
import com.g_wuy.swp391.voltera.model.response.ComplaintResponse;
import com.g_wuy.swp391.voltera.repository.AccountRepository;
import com.g_wuy.swp391.voltera.repository.ComplaintRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestHeader;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ComplaintService {

    ComplaintRepository complaintRepository;

    UserService userService;

    JwtService jwtService;

    ComplaintMapper complaintMapper;

    AccountRepository accountRepository;

    public Complaint createComplaint(ComplaintRequest request, @RequestHeader("Authorization") String auth) {
        String token = auth.substring(7);
        if (token == null) {
            throw new BusinessException("Can't get token");
        }
        User sender = userService.findUserByUsername(jwtService.extractUsername(token));
        if (sender == null) {
            throw new BusinessException("User not found");
        }
        Complaint complaint = complaintMapper.toComplaint(request);
        complaint.setSenderId(sender);
        complaint.setStatus("PENDING");
        complaint.setCreateAt(LocalDateTime.now());
        complaintRepository.save(complaint);
        return complaint;
    }

    public List<Complaint> getComplaintByStatus(String status, @RequestHeader("Authorization") String auth) {
        String token = auth.substring(7);
        Optional<Account> account = accountRepository.findByUsername(jwtService.extractUsername(token));
        if (!account.get().getRole().equals("ADMIN")) {
            throw new BusinessException("You don't have permission to do this action");
        }
        return complaintRepository.getComplaintsByStatusIgnoreCase(status);
    }

    public List<Complaint> viewComplaintByUserId(@RequestHeader("Authorization") String auth) {
        String token = auth.substring(7);
        User sender = userService.findUserByUsername(jwtService.extractUsername(token));
        if (sender == null) {
            throw new BusinessException("User not found");
        }
        return complaintRepository.viewComplaintByUserId(sender.getId());
    }

    public Optional<Complaint> detailComplaint(Integer complaintId) {
        Optional<Complaint> complaint = complaintRepository.findById(complaintId);
        return complaint;
    }

    public ResponseEntity<Complaint> updateComplaintStatusById(Integer complaintId, String status) {
        System.out.println("🔄 Updating complaint ID: " + complaintId + " to status: " + status);

        Complaint complaint = complaintRepository.findComplaintById(complaintId);
        if (complaint == null) {
            System.out.println("❌ Complaint not found with ID: " + complaintId);
            throw new BusinessException("Complaint not found");
        }

        System.out.println("📄 Found complaint - Current status: " + complaint.getStatus());

        complaint.setStatus(status);

        // Nếu resolve thì cập nhật resolveAt timestamp
        if ("RESOLVED".equals(status)) {
            complaint.setResolveAt(LocalDateTime.now());
            System.out.println("✅ Setting resolveAt timestamp");
        }

        Complaint savedComplaint = complaintRepository.save(complaint);
        System.out.println("✅ Complaint updated successfully - New status: " + savedComplaint.getStatus());

        return ResponseEntity.ok(savedComplaint);
    }
}