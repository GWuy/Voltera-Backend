package com.g_wuy.swp391.voltera.controller;

import com.g_wuy.swp391.voltera.model.request.PayOSRequest;
import com.g_wuy.swp391.voltera.model.response.PayOSCreateResponse;
import com.g_wuy.swp391.voltera.service.PayOSService;
import jakarta.servlet.http.HttpServletRequest;
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
    public ResponseEntity<PayOSCreateResponse> createPayment(@PathVariable Integer transactionId, @RequestBody(required = false) PayOSRequest request, HttpServletRequest httpRequest) {
        return ResponseEntity.ok(payOSService.createPayment(transactionId, request, httpRequest));
    }

    @GetMapping("/return/{transactionId}")
    public ResponseEntity<Void> returnUrl(@PathVariable Integer transactionId) {
        return ResponseEntity.status(302).header("Location", "voltera://payment/callback?transactionId=" + transactionId).build();
    }

    @GetMapping("/cancel/{transactionId}")
    public ResponseEntity<Void> cancelUrl(@PathVariable Integer transactionId) {
        return ResponseEntity.status(302).header("Location", "voltera://payment/callback?transactionId=" + transactionId + "&status=cancelled").build();
    }

    @PostMapping("/webhook")
    public ResponseEntity<Map<String, Object>> webhook(@RequestBody Object body) {
        log.info("PAYOS WEBHOOK RECEIVED: {}", body);
        WebhookData data = payOS.webhooks().verify(body);
        log.info(
                "orderCode={}, code={}",
                data.getOrderCode(),
                data.getCode()
        );
        payOSService.processWebhook(data);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
