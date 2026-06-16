package com.g_wuy.swp391.voltera.service;

import com.g_wuy.swp391.voltera.entity.*;
import com.g_wuy.swp391.voltera.model.request.PayOSRequest;
import com.g_wuy.swp391.voltera.model.response.PayOSResponse;
import com.g_wuy.swp391.voltera.repository.*;
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

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PayOSService {

    PayOS payOS;
    TransactionRepository transactionRepository;
    PaymentRepository paymentRepository;
    FeeRepository feeRepository;
    NotificationService notificationService;
    UserRepository userRepository;
    VehicleRepository vehicleRepository;
    BatteryRepository batteryRepository;
    BankRepository bankRepository;
    BankTransferRepository bankTransferRepository;
    PostRepository postRepository;

    public PayOSResponse createPayment(PayOSRequest request, HttpServletRequest httpRequest, Integer transactionId) {
        try {
            final String baseUrl = getBaseUrl(httpRequest);
            final String returnUrl = baseUrl + "/api/payos/return/" + transactionId;
            final String cancelUrl = baseUrl + "/api/payos/cancel/" + transactionId;
            final long orderCode = Long.parseLong(transactionId + String.valueOf(System.currentTimeMillis()).substring(7));
            final long price = (long) request.getAmount();

            PaymentLinkItem item = PaymentLinkItem.builder()
                    .name(request.getOrderInfo())
                    .price(price)
                    .quantity(1)
                    .build();

            CreatePaymentLinkRequest paymentData = CreatePaymentLinkRequest.builder()
                    .orderCode(orderCode)
                    .amount(price)
                    .description(request.getOrderInfo())
                    .returnUrl(returnUrl)
                    .cancelUrl(cancelUrl)
                    .item(item)
                    .build();

            CreatePaymentLinkResponse data = payOS.paymentRequests().create(paymentData);

            return PayOSResponse.builder()
                    .code("00")
                    .message("success")
                    .paymentUrl(data.getCheckoutUrl())
                    .build();
        } catch (Exception e) {
            log.error("Error creating PayOS payment: ", e);
            return PayOSResponse.builder()
                    .code("99")
                    .message("Error: " + e.getMessage())
                    .build();
        }
    }

    public String handleReturn(Map<String, String> params, Integer transactionId) {
        try {
            String code = params.get("code");
            String id = params.get("id");
            String orderCodeStr = params.get("orderCode");

            Transaction transaction = transactionRepository.findById(transactionId)
                    .orElseThrow(() -> new RuntimeException("Transaction not found"));

            if ("00".equals(code)) {
                return processSuccessfulPayment(transaction, params);
            } else {
                return processFailedPayment(transaction, params);
            }
        } catch (Exception e) {
            log.error("Error handling PayOS return", e);
            return "Lỗi xử lý callback: " + e.getMessage();
        }
    }
    
    public void processWebhook(WebhookData data) {
         try {
             log.info("Received PayOS webhook for order: {}", data.getOrderCode());
         } catch (Exception e) {
             log.error("Webhook processing error", e);
         }
    }

    private String processSuccessfulPayment(Transaction transaction, Map<String, String> params) {
        Fee fee = feeRepository.findByTransactionId(transaction.getTransactionid());
        
        // We will mock amount based on transaction. In production, we'd verify the amount from payOS API.
        BigDecimal amount = transaction.getPrice() != null ? transaction.getPrice() : BigDecimal.ZERO;

        transaction.setUpdateAt(Instant.now());

        Payment payment = new Payment();
        payment.setTransaction(transaction);
        payment.setPaymentMethod("PAYOS");
        payment.setTransactionCode(params.get("orderCode"));
        payment.setPaymentDate(LocalDateTime.now());
        payment.setAmount(amount);
        // If not present in params, assume from what we have
        payment.setOrderInfo("Thanh toan PayOS cho ma GD: " + params.get("orderCode"));

        transaction.setTransactionStatus("DONE");
        payment.setPaymentStatus("COMPLETED");
        
        Post post = transaction.getPost();
        
        // Cùng logic như VNPay
        if (post != null) {
            boolean isContractPayment = transaction.getContractid() != null;
            
            if (!isContractPayment) {
                 // FEE PAYMENT
                 post.setStatus("PENDING");  
                 if (fee != null) fee.setFeeStatus("PAID");
                 log.info("PayOS Fee payment successful - Post status: PENDING, Fee status: PAID");
            } else {
                 // CONTRACT PAYMENT
                 post.setStatus("SOLD");
                 if (fee != null) fee.setFeeStatus("PAID");
                 
                 if (post.getVehicle() != null) {
                     post.getVehicle().setStatus("SOLD");
                     vehicleRepository.save(post.getVehicle());
                 }
                 
                 if (post.getBattery() != null) {
                     batteryRepository.save(post.getBattery());
                 }
                 
                 User seller = post.getSellerId();
                 Bank bankSeller = bankRepository.findBankByUserId(seller.getId());
                 
                 if (bankSeller != null) {
                     BigDecimal balance = bankSeller.getBalance() != null ? bankSeller.getBalance() : BigDecimal.ZERO;
                     bankSeller.setBalance(balance.add(amount));
                     bankRepository.save(bankSeller);
                 }
                 
                 User buyer = transaction.getBuyerid();
                 if (bankSeller != null && buyer != null) {
                      BankTransfer bankTransfer = BankTransfer.builder()
                            .payment(payment)
                            .transaction(transaction)
                            .seller(seller)
                            .bank(bankSeller)
                            .amount(amount)
                            .transferStatus("COMPLETED")
                            .initiatedAt(Instant.now())
                            .completedAt(Instant.now())
                            .description("PayOS Transfer from " + buyer.getFullname() + " in posting " + post.getTitle())
                            .build();
                      bankTransferRepository.save(bankTransfer);
                 }
                 log.info("PayOS Contract payment successful - Post status: SOLD, Money transferred to seller");
            }
            postRepository.save(post);
        }
        
        paymentRepository.save(payment);
        transactionRepository.save(transaction);
        if (fee != null) feeRepository.save(fee);
        
        notificationService.sendForEvent(payment);
        notificationService.sendForEvent(transaction);
        
        return "Giao dịch thành công!";
    }
    
    private String processFailedPayment(Transaction transaction, Map<String, String> params) {
         Fee fee = feeRepository.findByTransactionId(transaction.getTransactionid());
         
         transaction.setUpdateAt(Instant.now());
         transaction.setTransactionStatus("FAILED");
         
         Payment payment = new Payment();
         payment.setTransaction(transaction);
         payment.setPaymentMethod("PAYOS");
         payment.setTransactionCode(params.get("orderCode"));
         payment.setPaymentDate(LocalDateTime.now());
         payment.setPaymentStatus("FAILED");
         payment.setAmount(transaction.getPrice() != null ? transaction.getPrice() : BigDecimal.ZERO);
         payment.setOrderInfo("Thanh toan PayOS that bai cho ma GD: " + params.get("orderCode"));
         
         if (fee != null) {
             fee.setFeeStatus("PENDING");
             feeRepository.save(fee);
         }
         
         paymentRepository.save(payment);
         transactionRepository.save(transaction);
         
         return "Giao dịch thất bại!";
    }

    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();

        String url = scheme + "://" + serverName;
        if ((scheme.equals("http") && serverPort != 80)
                || (scheme.equals("https") && serverPort != 443)) {
            url += ":" + serverPort;
        }
        url += contextPath;
        return url;
    }
}
