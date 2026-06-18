package com.g_wuy.swp391.voltera.service;

import com.g_wuy.swp391.voltera.entity.Payment;
import com.g_wuy.swp391.voltera.entity.Transaction;
import com.g_wuy.swp391.voltera.model.request.PayOSRequest;
import com.g_wuy.swp391.voltera.model.response.PayOSCreateResponse;
import com.g_wuy.swp391.voltera.repository.PaymentRepository;
import com.g_wuy.swp391.voltera.repository.TransactionRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;
import vn.payos.model.webhooks.WebhookData;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PayOSService {
    PayOS payOS;
    TransactionRepository transactionRepository;
    PaymentRepository paymentRepository;

    public PayOSCreateResponse createPayment(Integer transactionId, PayOSRequest request, HttpServletRequest httpRequest) {
        Transaction transaction = transactionRepository.findById(transactionId).orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));
        BigDecimal amount = transaction.getPrice() != null ? transaction.getPrice() : BigDecimal.ZERO;
        String orderInfo = request != null && request.getOrderInfo() != null ? request.getOrderInfo() : "Voltera transaction #" + transactionId;
        long orderCode = Long.parseLong(transactionId + String.valueOf(System.currentTimeMillis()).substring(7));
        PaymentLinkItem item = PaymentLinkItem.builder().name(orderInfo).price(amount.longValue()).quantity(1).build();
        CreatePaymentLinkRequest payload = CreatePaymentLinkRequest.builder().orderCode(orderCode).amount(amount.longValue()).description(orderInfo).returnUrl("voltera://payment/callback").cancelUrl("voltera://payment/callback?status=cancelled").item(item).build();
        CreatePaymentLinkResponse created = payOS.paymentRequests().create(payload);
        Payment payment = Payment.builder().transaction(transaction).paymentMethod("PAYOS").paymentStatus("PENDING").transactionCode(String.valueOf(orderCode)).amount(amount).orderInfo(orderInfo).paymentDate(LocalDateTime.now()).build();
        paymentRepository.save(payment);
        return PayOSCreateResponse.builder().checkoutUrl(created.getCheckoutUrl()).paymentLinkId((created.getPaymentLinkId())).orderCode(orderCode).transactionId(transactionId).build();
    }

    public void processWebhook(WebhookData data) {
        Integer transactionId = extractTransactionId(String.valueOf(data.getOrderCode()));
        Transaction transaction = transactionRepository.findById(transactionId).orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));
        if ("PAID".equalsIgnoreCase(transaction.getTransactionStatus()) || "FAILED".equalsIgnoreCase(transaction.getTransactionStatus()) || "CANCELLED".equalsIgnoreCase(transaction.getTransactionStatus()) || "DONE".equalsIgnoreCase(transaction.getTransactionStatus()))
            return;
        String status = "00".equals(data.getCode()) ? "PAID" : "FAILED";
        transaction.setTransactionStatus(status);
        transaction.setUpdateAt(Instant.now());
        transactionRepository.save(transaction);
    }

    public PayOSCreateResponse getPaymentStatus(Integer transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId).orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));
        return PayOSCreateResponse.builder().transactionId(transactionId).checkoutUrl(null).orderCode(null).paymentLinkId(null).build();
    }

    private Integer extractTransactionId(String orderCode) {
        return Integer.parseInt(orderCode.substring(0, Math.max(1, orderCode.length() - 3)));
    }
}