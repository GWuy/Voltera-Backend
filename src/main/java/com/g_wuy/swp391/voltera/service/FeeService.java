package com.g_wuy.swp391.voltera.service;

import com.g_wuy.swp391.voltera.configuration.VNPayConfiguration;
import com.g_wuy.swp391.voltera.entity.Fee;
import com.g_wuy.swp391.voltera.entity.Payment;
import com.g_wuy.swp391.voltera.entity.Post;
import com.g_wuy.swp391.voltera.entity.Transaction;
import com.g_wuy.swp391.voltera.exception.BusinessException;
import com.g_wuy.swp391.voltera.model.request.VNPayRequest;
import com.g_wuy.swp391.voltera.model.response.VNPayResponse;
import com.g_wuy.swp391.voltera.repository.FeeRepository;
import com.g_wuy.swp391.voltera.repository.PaymentRepository;
import com.g_wuy.swp391.voltera.repository.PostRepository;
import com.g_wuy.swp391.voltera.repository.TransactionRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class FeeService {
    static Logger log = LoggerFactory.getLogger(FeeService.class);

    FeeRepository feeRepository;

    PostRepository postRepository;

    TransactionRepository transactionRepository;

    VNPayService vNPayService;

    VNPayConfiguration vnPayConfig;

    PaymentRepository paymentRepository;

    public VNPayResponse createFeeAndPay(Integer transactionId, HttpServletRequest httpRequest) {

        Transaction transaction = transactionRepository.findById(transactionId).orElse(null);
        if (transaction == null) {
            throw new BusinessException("Transaction Not Found");
        }

        // 🔍 Check if Fee already exists for this post to avoid duplicates
        Fee existingFee = feeRepository.findValidFeeByPostId(transaction.getPost().getId()).orElse(null);

        Fee fee;
        if (existingFee != null && "PENDING".equals(existingFee.getFeeStatus())) {
            // ✅ Use existing Fee instead of creating duplicate
            fee = existingFee;
        } else {
            // ✨ Create new Fee only if none exists or existing is not PENDING
            fee = new Fee();
            fee.setPost(transaction.getPost());
            if (transaction.getPost().getVehicle() != null && transaction.getPost().getBattery() == null) {
                fee.setAmount(BigDecimal.valueOf(500000));
            } else if (transaction.getPost().getBattery() != null && transaction.getPost().getVehicle() == null) {
                fee.setAmount(BigDecimal.valueOf(200000));
            } else {
                throw new BusinessException("Post không có vehicle hay battery");
            }
            fee.setDescription("Fee for posting " + transaction.getPost().getTitle());
            fee.setCreatedAt(LocalDateTime.now());
            fee.setExpiredAt(LocalDateTime.now().plusDays(15));
            fee.setFeeStatus("PENDING");
            fee.setTransaction(transaction);
            feeRepository.save(fee);
        }

        VNPayRequest vnPayRequest = new VNPayRequest();
        vnPayRequest.setAmount(fee.getAmount().longValue());
        vnPayRequest.setPostId(transaction.getPost().getId());
        vnPayRequest.setOrderInfo("Pay for posting " + transaction.getPost().getTitle());
        vnPayRequest.setOrderType("Bill Payment");
        vnPayRequest.setBankCode("NCB");
        vnPayRequest.setLanguage("vn");

        return vNPayService.createPayment(vnPayRequest, httpRequest, transaction.getTransactionid());
    }

    public VNPayResponse renewFee(Integer postId, HttpServletRequest httpRequest) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Fee oldFee = feeRepository.findValidFeeByPostId(postId)
                .orElse(null);

        if (oldFee != null) {
            oldFee.setFeeStatus("EXPIRED");
            feeRepository.save(oldFee);
        }
        Fee newFee = Fee.builder()
                .post(post)
                .amount(BigDecimal.valueOf(50000))
                .description("Gia hạn bài đăng: " + post.getTitle())
                .feeStatus("PENDING")
                .createdAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusDays(15))
                .build();
        feeRepository.save(newFee);

        Transaction transaction = new Transaction();
        transaction.setPost(post);
        transaction.setTransactionStatus("APPROVE");
        transaction.setPrice(newFee.getAmount());
        transaction.setCreateAt(Instant.now());
        transactionRepository.save(transaction);

        newFee.setTransaction(transaction);
        feeRepository.save(newFee);

        VNPayRequest vnPayRequest = VNPayRequest.builder()
                .amount(newFee.getAmount().longValue())
                .orderInfo("Gia hạn bài đăng: " + post.getTitle())
                .build();

        return vNPayService.createPayment(vnPayRequest, httpRequest, transaction.getTransactionid());
    }

    public String handleReturn(Map<String, String> params, Integer transactionId) {
        try {
            String vnpSecureHash = params.get("vnp_SecureHash");
            params.remove("vnp_SecureHash");
            params.remove("vnp_SecureHashType");

            String signValue = vnPayConfig.hashAllFields(params);
            if (!signValue.equalsIgnoreCase(vnpSecureHash)) {
                log.error("Invalid checksum. Expected {}, got {}", vnpSecureHash, signValue);
                return "Lỗi xác minh chữ ký!";
            }

            Transaction transaction = transactionRepository.findById(transactionId)
                    .orElseThrow(() -> new RuntimeException("Transaction not found"));

            Fee fee = feeRepository.findByTransactionId(transactionId);
            if (fee == null) {
                log.error("❌ No fee found for transaction ID: {}", transactionId);
                throw new RuntimeException("Fee record not found for transaction: " + transactionId);
            }

            log.info("🔍 Found fee ID: {} for transaction ID: {}, current status: {}", fee.getId(), transactionId, fee.getFeeStatus());

            BigDecimal amount = new BigDecimal(params.get("vnp_Amount")).divide(BigDecimal.valueOf(100));
            transaction.setPrice(amount);
            transaction.setUpdateAt(Instant.now());

            Payment payment = new Payment();
            payment.setTransaction(transaction);
            payment.setPaymentMethod("VNPAY");
            payment.setTransactionCode(params.get("vnp_TxnRef"));
            payment.setPaymentDate(LocalDateTime.now());
            payment.setVnpTransactionNo(params.get("vnp_TransactionNo"));
            payment.setVnpBankCode(params.get("vnp_BankCode"));
            payment.setVnpBankTranNo(params.get("vnp_BankTranNo"));
            payment.setVnpCardType(params.get("vnp_CardType"));
            payment.setVnpPayDate(params.get("vnp_PayDate"));
            payment.setVnpResponseCode(params.get("vnp_ResponseCode"));
            payment.setAmount(amount);
            payment.setOrderInfo(params.get("vnp_OrderInfo"));

            if ("00".equals(params.get("vnp_ResponseCode"))) {
                transaction.setTransactionStatus("DONE");
                payment.setPaymentStatus("COMPLETED");

                // Keep post in PENDING status for admin review after payment
                Post post = transaction.getPost();
                post.setStatus("PENDING");
                postRepository.save(post);

                fee.setFeeStatus("PAID");
                log.info("✅ Payment successful for transaction ID: {}, post ID: {}, fee status updated to PAID", transactionId, post.getId());
            } else {
                transaction.setTransactionStatus("FAILED");
                payment.setPaymentStatus("FAILED");
                fee.setFeeStatus("PENDING");
                log.warn("❌ Payment failed for transaction ID: {}, response code: {}", transactionId, params.get("vnp_ResponseCode"));
            }

            // 🔧 Save all entities to persist changes
            paymentRepository.save(payment);
            transactionRepository.save(transaction);
            feeRepository.save(fee); // 🚀 CRITICAL: Save fee to persist feeStatus change

            log.info("💾 All entities saved - Fee ID: {}, new fee status: {}", fee.getId(), fee.getFeeStatus());

            return "Giao dịch " + transaction.getTransactionStatus().toLowerCase() + "!";
        } catch (Exception e) {
            FeeService.log.error("Error handling VNPay return", e);
            return "Lỗi xử lý callback: " + e.getMessage();
        }
    }

    // Admin methods for fee management
    public List<Object> getAllFeesForAdmin() {
        try {
            List<Fee> fees = feeRepository.findAll();
            return fees.stream()
                    .map(this::mapFeeToAdminView)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting all fees for admin", e);
            return new ArrayList<>();
        }
    }

    public Map<String, Object> getFeeStatistics() {
        try {
            List<Fee> allFees = feeRepository.findAll();

            long totalFees = allFees.size();
            long paidFees = allFees.stream().filter(f -> "PAID".equals(f.getFeeStatus())).count();
            long pendingFees = allFees.stream().filter(f -> "PENDING".equals(f.getFeeStatus())).count();
            long cancelledFees = allFees.stream().filter(f -> "CANCELLED".equals(f.getFeeStatus())).count();
            long expiredFees = allFees.stream().filter(f -> isExpired(f)).count();


            BigDecimal totalRevenue = allFees.stream()
                    .filter(f -> "PAID".equals(f.getFeeStatus()))
                    .map(Fee::getAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalFees", totalFees);
            stats.put("paidFees", paidFees);
            stats.put("pendingFees", pendingFees);
            stats.put("cancelledFees", cancelledFees);
            stats.put("expiredFees", expiredFees);
            stats.put("totalRevenue", totalRevenue);

            return stats;
        } catch (Exception e) {
            log.error("Error calculating fee statistics", e);
            return new HashMap<>();
        }
    }

    private Object mapFeeToAdminView(Fee fee) {
        Map<String, Object> feeView = new HashMap<>();
        feeView.put("id", fee.getId());
        feeView.put("amount", fee.getAmount());
        feeView.put("status", fee.getFeeStatus());
        feeView.put("createdAt", fee.getCreatedAt());
        feeView.put("expiredAt", fee.getExpiredAt());
        feeView.put("description", fee.getDescription());

        // Add post information if available
        if (fee.getPost() != null) {
            Map<String, Object> postInfo = new HashMap<>();
            postInfo.put("id", fee.getPost().getId());
            postInfo.put("title", fee.getPost().getTitle());
            feeView.put("post", postInfo);
        }

        // Add transaction information if available
        if (fee.getTransaction() != null) {
            Map<String, Object> transactionInfo = new HashMap<>();
            transactionInfo.put("transactionid", fee.getTransaction().getTransactionid());
            feeView.put("transaction", transactionInfo);
        }

        return feeView;
    }

    private boolean isExpired(Fee fee) {
        if (fee.getExpiredAt() == null) return false;
        return fee.getExpiredAt().isBefore(LocalDateTime.now());
    }
}
