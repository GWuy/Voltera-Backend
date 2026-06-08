package com.g_wuy.swp391.voltera.controller;

import com.g_wuy.swp391.voltera.model.response.VNPayResponse;
import com.g_wuy.swp391.voltera.service.FeeService;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fee")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FeeController {

    FeeService feeService;

    @PostMapping("/create-payment/{transactionId}")
    public ResponseEntity<VNPayResponse> createFeeAndPay(
            @PathVariable Integer transactionId,
            HttpServletRequest request) {
        VNPayResponse response = feeService.createFeeAndPay(transactionId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/renew/{postId}")
    public ResponseEntity<VNPayResponse> renewFee(
            @PathVariable Integer postId,
            HttpServletRequest request) {
        VNPayResponse response = feeService.renewFee(postId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<Object>> getAllFees() {
        try {
            List<Object> fees = feeService.getAllFeesForAdmin();
            return ResponseEntity.ok(fees);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/admin/stats")
    public ResponseEntity<Map<String, Object>> getFeeStats() {
        try {
            Map<String, Object> stats = feeService.getFeeStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
