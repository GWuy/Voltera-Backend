package com.g_wuy.swp391.voltera.controller;

import com.g_wuy.swp391.voltera.model.response.RefundResponse;
import com.g_wuy.swp391.voltera.service.RefundService;
import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("api/refunds")
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RefundController {

    RefundService refundService;

    @PostMapping("/request-refund/{transactionId}")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<RefundResponse> createRefund(
            @RequestParam String reason,
            @PathVariable Integer transactionId,
            @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(refundService.createRefund(reason, transactionId, token).getBody());
    }

    @PostMapping("/{refundId}/images")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<?> uploadRefundImages(
            @PathVariable("refundId") Integer refundId,
            @RequestParam("images") MultipartFile[] images,
            @RequestHeader("Authorization") String token) {
        try {
            refundService.uploadRefundImages(refundId, images, token);
            return ResponseEntity.ok("Images uploaded successfully");
        } catch (Exception e) {
            log.error("Error uploading refund images", e);
            return ResponseEntity.internalServerError()
                    .body("Failed to upload images: " + e.getMessage());
        }
    }

    @GetMapping("/buyer/{refundStatus}")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<List<RefundResponse>> getAllRefundsBySenderAndStatus(
            @RequestHeader("Authorization") String token,
            @PathVariable("refundStatus") String status) {
        return ResponseEntity.ok(refundService.findRefundsBySenderId(token, status).getBody());
    }

    @GetMapping("/seller/{refundStatus}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<List<RefundResponse>> getAllRefundsByReceiverAndStatus(
            @RequestHeader("Authorization") String token,
            @PathVariable("refundStatus") String status) {
        return ResponseEntity.ok(refundService.findRefundsByReceiverId(token, status).getBody());
    }

    @PutMapping("/accept/{refundId}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<?> acceptRefund(
            @PathVariable Integer refundId,
            @RequestHeader("Authorization") String token) {
        try {
            refundService.acceptRefund(refundId, token);
            return ResponseEntity.ok("Refund accepted successfully");
        } catch (Exception e) {
            log.error("Error accepting refund", e);
            return ResponseEntity.internalServerError()
                    .body("Failed to accept refund: " + e.getMessage());
        }
    }

    @PutMapping("/reject/{refundId}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<?> rejectRefund(
            @PathVariable Integer refundId,
            @RequestHeader("Authorization") String token) {
        try {
            refundService.rejectRefund(refundId, token);
            return ResponseEntity.ok("Refund rejected successfully");
        } catch (Exception e) {
            log.error("Error rejecting refund", e);
            return ResponseEntity.internalServerError()
                    .body("Failed to reject refund: " + e.getMessage());
        }
    }

    @PostMapping("/payment/{refundId}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<?> createRefundPayment(
            @PathVariable Integer refundId,
            @RequestHeader("Authorization") String token,
            HttpServletRequest request) {
        try {
            String paymentUrl = refundService.createRefundPaymentUrl(refundId, token, request);
            return ResponseEntity.ok(Map.of("paymentUrl", paymentUrl));
        } catch (Exception e) {
            log.error("Error creating refund payment", e);
            return ResponseEntity.internalServerError()
                    .body("Failed to create refund payment: " + e.getMessage());
        }
    }

    @GetMapping("/payment/callback/{refundId}")
    public ResponseEntity<?> handleRefundPaymentCallback(
            @PathVariable Integer refundId,
            @RequestParam Map<String, String> params,
            @RequestHeader("Authorization") String token) {
        try {
            // Verify payment success from VNPay params
            String responseCode = params.get("vnp_ResponseCode");
            if (!"00".equals(responseCode)) {
                return ResponseEntity.badRequest()
                        .body("Payment failed or cancelled");
            }

            // Complete the refund
            refundService.completeRefundPayment(refundId, token);
            return ResponseEntity.ok("Refund payment completed successfully");

        } catch (Exception e) {
            log.error("Error handling refund payment callback", e);
            return ResponseEntity.internalServerError()
                    .body("Failed to complete refund payment: " + e.getMessage());
        }
    }

    @PostMapping("/claim-money/{refundId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> claimRefundMoney(
            @PathVariable Integer refundId,
            @RequestHeader("Authorization") String token) {
        try {
            log.info("Attempting to claim refund money for refund ID: {}", refundId);
            String result = refundService.claimRefundMoney(refundId, token);
            return ResponseEntity.ok().body(result);
        } catch (Exception e) {
            log.error("Error claiming refund money for refund ID {}: {}", refundId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Failed to claim refund money: " + e.getMessage());
        }
    }

    @GetMapping("/is-claimed/{refundId}")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<?> isRefundClaimed(
            @PathVariable Integer refundId,
            @RequestHeader("Authorization") String token) {
        try {
            boolean isClaimed = refundService.isRefundClaimed(refundId, token);
            return ResponseEntity.ok().body(isClaimed);
        } catch (Exception e) {
            log.error("Error checking refund claim status for refund ID {}: {}", refundId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Failed to check refund status: " + e.getMessage());
        }
    }

    @PutMapping("/update-status/{refundId}/{status}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<RefundResponse> updateRefundStatus(
            @PathVariable Integer refundId,
            @PathVariable String status,
            @RequestHeader("Authorization") String token) {
        return refundService.updateRefundStatusAndGetMoneyFromSeller(refundId, status, token);
    }

    // Admin endpoints
    @GetMapping("/admin/buyer/{refundStatus}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RefundResponse>> getAdminBuyerRefunds(
            @RequestHeader("Authorization") String token,
            @PathVariable("refundStatus") String status) {
        return ResponseEntity.ok(refundService.findAllBuyerRefundsByStatus(status).getBody());
    }

    @GetMapping("/admin/seller/{refundStatus}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RefundResponse>> getAdminSellerRefunds(
            @PathVariable("refundStatus") String status) {
        return ResponseEntity.ok(refundService.findAllSellerRefundsByStatus(status).getBody());
    }

    @GetMapping("/admin/all/{refundStatus}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RefundResponse>> getAdminAllRefunds(
            @RequestHeader("Authorization") String token,
            @PathVariable("refundStatus") String status) {
        return ResponseEntity.ok(refundService.findAllRefundsByStatus(status).getBody());
    }

    @PutMapping("/admin/accept/{refundId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> adminAcceptRefund(
            @PathVariable Integer refundId,
            @RequestHeader("Authorization") String token) {
        try {
            refundService.adminAcceptRefund(refundId, token);
            return ResponseEntity.ok("Refund accepted successfully by admin");
        } catch (Exception e) {
            log.error("Error accepting refund by admin", e);
            return ResponseEntity.internalServerError()
                    .body("Failed to accept refund: " + e.getMessage());
        }
    }

    @PutMapping("/admin/reject/{refundId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> adminRejectRefund(
            @PathVariable Integer refundId,
            @RequestHeader("Authorization") String token) {
        try {
            refundService.adminRejectRefund(refundId, token);
            return ResponseEntity.ok("Refund rejected successfully by admin");
        } catch (Exception e) {
            log.error("Error rejecting refund by admin", e);
            return ResponseEntity.internalServerError()
                    .body("Failed to reject refund: " + e.getMessage());
        }
    }
}
