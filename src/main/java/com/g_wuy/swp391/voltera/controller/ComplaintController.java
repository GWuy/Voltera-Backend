package com.g_wuy.swp391.voltera.controller;

import com.g_wuy.swp391.voltera.entity.Complaint;
import com.g_wuy.swp391.voltera.mapper.ComplaintMapper;
import com.g_wuy.swp391.voltera.model.request.ComplaintRequest;
import com.g_wuy.swp391.voltera.model.response.ComplaintResponse;
import com.g_wuy.swp391.voltera.service.ComplaintService;
import jakarta.validation.Valid;

import java.util.List;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ComplaintController {

    ComplaintService complaintService;

    ComplaintMapper complaintMapper;

    @PostMapping
    public ResponseEntity<Complaint> createComplaint(
            @Valid
            @RequestBody ComplaintRequest request,
            @RequestHeader("Authorization") String auth) {
        Complaint complaint = complaintService.createComplaint(request, auth);
        return ResponseEntity.ok(complaint);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Complaint>> getComplaintByStatus(
            @PathVariable String status,
            @RequestHeader("Authorization") String auth) {
        List<Complaint> complaints = complaintService.getComplaintByStatus(status, auth);
        return ResponseEntity.ok(complaints);
    }

    @GetMapping("/my-complaints")
    public ResponseEntity<List<Complaint>> viewMyComplaints(
            @RequestHeader("Authorization") String auth) {
        List<Complaint> complaints = complaintService.viewComplaintByUserId(auth);
        return ResponseEntity.ok(complaints);
    }

    @GetMapping("/{complaintId}")
    public ResponseEntity<Complaint> detailsComplaint(@PathVariable Integer complaintId) {
        return complaintService.detailComplaint(complaintId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{complaintId}/{complaintStatus}")
    public ResponseEntity<ComplaintResponse> updateComplaintStatus(
            @PathVariable Integer complaintId,
            @PathVariable String complaintStatus) {
        return ResponseEntity.ok(complaintMapper.toComplaintResponse(complaintService.updateComplaintStatusById(complaintId, complaintStatus).getBody()));
    }
}
