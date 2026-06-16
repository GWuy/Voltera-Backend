package com.g_wuy.swp391.voltera.controller;

import com.g_wuy.swp391.voltera.model.request.PayOSRequest;
import com.g_wuy.swp391.voltera.model.response.PayOSResponse;
import com.g_wuy.swp391.voltera.service.PayOSService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.payos.PayOS;
import vn.payos.model.webhooks.WebhookData;

import java.util.Map;

@RestController
@RequestMapping("/api/payos")
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PayOSController {

    PayOSService payOSService;
    PayOS payOS;

    @PostMapping("/create-payment/{transactionId}")
    public ResponseEntity<PayOSResponse> createPayment(
            @RequestBody PayOSRequest request,
            HttpServletRequest httpRequest,
            @PathVariable Integer transactionId) {
        try {
            PayOSResponse response = payOSService.createPayment(request, httpRequest, transactionId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error creating PayOS payment: ", e);
            PayOSResponse errorResponse = new PayOSResponse();
            errorResponse.setCode("99");
            errorResponse.setMessage("Error creating payment: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/return/{transactionId}")
    public void handleReturn(
            @RequestParam Map<String, String> params,
            @PathVariable Integer transactionId,
            HttpServletRequest request,
            HttpServletResponse response) throws java.io.IOException {

        payOSService.handleReturn(params, transactionId);

        StringBuilder frontendUrl = new StringBuilder("http://localhost:5173/payment/callback");
        frontendUrl.append("?");

        for (Map.Entry<String, String> entry : params.entrySet()) {
            frontendUrl.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        frontendUrl.append("paymentMethod=PAYOS");

        response.sendRedirect(frontendUrl.toString());
    }

    @GetMapping("/cancel/{transactionId}")
    public void handleCancel(
            @RequestParam Map<String, String> params,
            @PathVariable("transactionId") Integer transactionId,
            HttpServletRequest request,
            HttpServletResponse response) throws java.io.IOException {

        params.put("code", "99"); // Fake a failed code for handling
        payOSService.handleReturn(params, transactionId);

        StringBuilder frontendUrl = new StringBuilder("http://localhost:5173/payment/callback");
        frontendUrl.append("?");

        for (Map.Entry<String, String> entry : params.entrySet()) {
            frontendUrl.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        frontendUrl.append("paymentMethod=PAYOS");

        response.sendRedirect(frontendUrl.toString());
    }

    @PostMapping("/webhook")
    public ResponseEntity<?> payosWebhookHandler(@RequestBody Object body) {
        try {
            WebhookData data = payOS.webhooks().verify(body);
            payOSService.processWebhook(data);
            return ResponseEntity.ok("Webhook delivered");
        } catch (Exception e) {
            log.error("Webhook error: ", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
