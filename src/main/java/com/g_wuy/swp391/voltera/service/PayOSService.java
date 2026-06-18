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
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PayOSService {
    PayOS payOS;
    TransactionRepository transactionRepository;
    PaymentRepository paymentRepository;

    public PayOSCreateResponse createPayment(Integer transactionId, PayOSRequest request) {
        Transaction transaction = transactionRepository.findById(transactionId).orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));
        BigDecimal amount = transaction.getPrice() != null ? transaction.getPrice() : BigDecimal.ZERO;
        String orderInfo = request != null && request.getOrderInfo() != null ? request.getOrderInfo() : "Voltera transaction #" + transactionId;
        long orderCode = Long.parseLong(transactionId + String.valueOf(System.currentTimeMillis()).substring(7));
        PaymentLinkItem item = PaymentLinkItem.builder().name(orderInfo).price(amount.longValue()).quantity(1).build();
        CreatePaymentLinkRequest payload = CreatePaymentLinkRequest.builder()
                .orderCode(orderCode)
                .amount(amount.longValue())
                .description(orderInfo)
                .returnUrl(
                        "voltera://payment/callback"
                                + "?transactionId=" + transactionId
                )
                .cancelUrl(
                        "voltera://payment/callback"
                                + "?transactionId=" + transactionId
                                + "&status=CANCELLED"
                )
                .item(item)
                .build();
        CreatePaymentLinkResponse created = payOS.paymentRequests().create(payload);
        Payment payment = Payment.builder().transaction(transaction).paymentMethod("PAYOS").paymentStatus("PENDING").transactionCode(String.valueOf(orderCode)).amount(amount).orderInfo(orderInfo).paymentDate(LocalDateTime.now()).build();
        paymentRepository.save(payment);
        return PayOSCreateResponse.builder().checkoutUrl(created.getCheckoutUrl()).paymentLinkId((created.getPaymentLinkId())).orderCode(orderCode).transactionId(transactionId).build();
    }

    public void processWebhook(WebhookData data) {
        log.info("Webhook orderCode={}", data.getOrderCode());
        Optional<Payment> paymentOpt =
                paymentRepository.findByTransactionCode(
                        String.valueOf(data.getOrderCode()));

        if (paymentOpt.isEmpty()) {
            log.warn("Payment not found. orderCode={}", data.getOrderCode());
            return;
        }

        Payment payment = paymentOpt.get();
        Transaction transaction = payment.getTransaction();

        if ("00".equals(data.getCode())) {

            payment.setPaymentStatus("SUCCESS");
            transaction.setTransactionStatus("APPROVE");

        } else {

            payment.setPaymentStatus("FAILED");
            transaction.setTransactionStatus("FAILED");
        }

        paymentRepository.save(payment);
        transactionRepository.save(transaction);
    }

    public Map<String, Object> syncTransactionStatus(Integer transactionId) {

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() ->
                        new RuntimeException("Transaction not found: " + transactionId));

        // Nếu transaction đã ở trạng thái cuối thì không xử lý nữa
        if (isFinalStatus(transaction.getTransactionStatus())) {
            return Map.of(
                    "success", true,
                    "transactionId", transactionId,
                    "transactionStatus", transaction.getTransactionStatus()
            );
        }

        Payment payment = paymentRepository.findPaymentByTransactionId(transactionId);

        if (payment != null) {

            String paymentStatus = payment.getPaymentStatus() == null
                    ? "PENDING"
                    : payment.getPaymentStatus().toUpperCase();

            switch (paymentStatus) {

                case "SUCCESS":
                case "COMPLETED":
                case "PAID":
                    transaction.setTransactionStatus("APPROVE");
                    break;

                case "FAILED":
                case "CANCELLED":
                    transaction.setTransactionStatus("FAILD");
                    break;

                default:
                    // PENDING -> giữ nguyên trạng thái hiện tại
                    break;
            }

            transaction.setUpdateAt(Instant.now());
            transactionRepository.save(transaction);
        }

        return Map.of(
                "success", true,
                "transactionId", transactionId,
                "transactionStatus", transaction.getTransactionStatus()
        );
    }

    public PayOSCreateResponse getPaymentStatus(Integer transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId).orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));
        return PayOSCreateResponse.builder().transactionId(transactionId).checkoutUrl(null).orderCode(null).paymentLinkId(null).build();
    }

    private boolean isFinalStatus(String status) {
        if (status == null) return false;
        String s = status.toUpperCase();
        return "APPROVE".equals(s) || "FAILD".equals(s) || "DONE".equals(s) || "FAILED".equals(s) || "CANCELLED".equals(s);
    }
}