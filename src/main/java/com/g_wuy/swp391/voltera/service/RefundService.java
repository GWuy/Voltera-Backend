package com.g_wuy.swp391.voltera.service;

import com.g_wuy.swp391.voltera.entity.*;
import com.g_wuy.swp391.voltera.exception.BusinessException;
import com.g_wuy.swp391.voltera.mapper.RefundMapper;
import com.g_wuy.swp391.voltera.model.response.RefundResponse;
import com.g_wuy.swp391.voltera.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@Slf4j
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class RefundService {


    UserRepository userRepository;

    RefundRepository refundRepository;

    TransactionRepository transactionRepository;

    JwtService jwtService;

    RefundMapper refundMapper;

    BankRepository bankRepository;

    PaymentRepository paymentRepository;

    BankTransferRepository bankTransferRepository;

    RefundImageRepository refundImageRepository;

    VNPayService vnPayService;

    NotificationService notificationService;


    public ResponseEntity<RefundResponse> createRefund(String reason, Integer transactionId, @RequestHeader("Authorization") String token) {
        User sender = getUserByToken(token);
        log.info("Creating refund: senderId={}, transactionId={}, reason={}", sender.getId(), transactionId, reason);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new BusinessException("Transaction not found"));

        log.info("Transaction found: id={}, status={}", transaction.getTransactionid(), transaction.getTransactionStatus());

        // Check transaction status - must be DONE to allow refund creation
        if (!"DONE".equalsIgnoreCase(transaction.getTransactionStatus())) {
            if ("FAILED".equalsIgnoreCase(transaction.getTransactionStatus())) {
                log.warn("Transaction {} has FAILED status - already refunded", transactionId);
                throw new BusinessException("This transaction has already been refunded");
            } else {
                log.warn("Transaction status is not DONE: {}", transaction.getTransactionStatus());
                throw new BusinessException("This transaction haven't been completed yet");
            }
        }

        // Check if there's already an existing refund for this transaction
        List<Refund> existingRefunds = refundRepository.findByTransaction(transaction);
        if (!existingRefunds.isEmpty()) {
            Refund existingRefund = existingRefunds.get(0);
            log.warn("Refund already exists for transaction {}: refundId={}, status={}",
                    transactionId, existingRefund.getId(), existingRefund.getRefundStatus());

            String userMessage;
            switch (existingRefund.getRefundStatus().toUpperCase()) {
                case "REQUESTED":
                    userMessage = "A refund request is already pending for this transaction. Please wait for seller approval.";
                    break;
                case "APPROVED":
                    userMessage = "A refund has been approved for this transaction. You can claim your money on the refund page.";
                    break;
                case "REJECTED":
                    userMessage = "A refund request for this transaction was previously rejected by the seller.";
                    break;
                case "REFUNDED":
                    userMessage = "This transaction has already been fully refunded. No further refund is possible.";
                    break;
                default:
                    userMessage = "A refund request already exists for this transaction with status: " + existingRefund.getRefundStatus();
            }

            throw new BusinessException(userMessage);
        }

        User receiver = transaction.getPost().getSellerId();
        if (receiver == null) {
            throw new BusinessException("Receiver not found");
        }
        Refund refund = Refund.builder()
                .transaction(transaction)
                .sender(sender)
                .receiver(receiver)
                .amount(transaction.getPrice())
                .reason(reason)
                .refundStatus("REQUESTED")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        refundRepository.save(refund);
        return ResponseEntity.ok(refundMapper.toRefundResponse(refund));
    }

    public ResponseEntity<List<RefundResponse>> findRefundsBySenderId(@RequestHeader("Authorization") String token, String status) {
        List<RefundResponse> refundResponses;

        User sender = getUserByToken(token);

        if ("".equalsIgnoreCase(status) || status == null || "ALL".equalsIgnoreCase(status)) {
            refundResponses = refundRepository.getAllRefundBySenderId(sender.getId());
        } else {
            refundResponses = refundRepository.getRefundBySenderIdAndStatus(sender.getId(), status);
        }
        return ResponseEntity.ok(refundResponses);
    }

    public ResponseEntity<List<RefundResponse>> findRefundsByReceiverId(@RequestHeader("Authorization") String token, String status) {
        List<RefundResponse> refundResponses;
        User receiver = getUserByToken(token);


        if ("".equalsIgnoreCase(status) || status == null || "ALL".equalsIgnoreCase(status)) {
            refundResponses = refundRepository.getRefundByReceiverId(receiver.getId());
        } else {
            refundResponses = refundRepository.getRefundByReceiverIdAndStatus(receiver.getId(), status);
        }
        return ResponseEntity.ok(refundResponses);
    }

    public boolean isRefundClaimed(Integer refundId, String token) {
        User user = getUserByToken(token);

        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new BusinessException("Refund not found"));

        // Verify user owns this refund
        if (!refund.getSender().getId().equals(user.getId())) {
            throw new BusinessException("You don't have permission to check this refund");
        }

        // Check if refund has been claimed by checking claimedAt field
        return refund.getClaimedAt() != null;
    }

    public ResponseEntity<RefundResponse> updateRefundStatusAndGetMoneyFromSeller(
            Integer refundId,
            String status,
            @RequestHeader("Authorization") String token) {

        if (status == null || status.isBlank()) {
            throw new BusinessException("Status must not be empty");
        }

        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new BusinessException("Refund not found"));

        User user = getUserByToken(token);
        User seller = refund.getReceiver();

        if (!seller.equals(user)) {
            throw new BusinessException("You are not the seller of this transaction");
        }

        List<String> validStatuses = List.of("APPROVED", "REJECTED");
        if (!validStatuses.contains(status.toUpperCase())) {
            throw new BusinessException("Invalid refund status");
        }

        refund.setRefundStatus(status.toUpperCase());
        refund.setUpdatedAt(Instant.now());
        refundRepository.save(refund);

        if ("APPROVED".equalsIgnoreCase(status)) {
            Bank bankSeller = bankRepository.findBankByUserId(seller.getId());
            if (bankSeller == null) {
                throw new BusinessException("Seller bank account not found");
            }

            BigDecimal amount = refund.getTransaction().getPrice();
            BigDecimal balance = bankSeller.getBalance();

            if (balance.compareTo(amount) < 0) {
                throw new BusinessException("Seller does not have enough balance to approve refund");
            }

            bankSeller.setBalance(balance.subtract(amount));
            bankRepository.save(bankSeller);

            BankTransfer bankTransfer = BankTransfer.builder()
                    .payment(paymentRepository.findPaymentByTransactionId(
                            refund.getTransaction().getTransactionid()))
                    .transaction(refund.getTransaction())
                    .seller(seller)
                    .bank(bankSeller)
                    .amount(amount)
                    .transferStatus("COMPLETED")
                    .initiatedAt(Instant.now())
                    .completedAt(Instant.now())
                    .description("Refund for " + refund.getSender().getFullname() + "ith posting " + refund.getTransaction().getPost().getTitle())
                    .build();
            bankTransferRepository.save(bankTransfer);
        }

        return ResponseEntity.ok(refundMapper.toRefundResponse(refund));
    }

    public User getUserByToken(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new BusinessException("Invalid token format");
        }

        String jwt = token.substring(7);
        String username = jwtService.extractUsername(jwt);
        if (username == null) {
            throw new BusinessException("Invalid token");
        }

        return userRepository.findUserByUsername(username);
    }

    public void uploadRefundImages(Integer refundId, MultipartFile[] images, String token) {
        User user = getUserByToken(token);

        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new BusinessException("Refund not found"));

        // Verify user owns this refund
        if (!refund.getSender().getId().equals(user.getId())) {
            throw new BusinessException("You don't have permission to upload images for this refund");
        }

        // Save images (simplified - in real app you'd upload to cloud storage)
        for (MultipartFile image : images) {
            if (!image.isEmpty()) {
                RefundImage refundImage = new RefundImage();
                refundImage.setRefund(refund);
                refundImage.setImageUrl("uploads/refunds/" + refundId + "/" + image.getOriginalFilename());
                refundImage.setUploadedAt(Instant.now());
                refundImageRepository.save(refundImage);
            }
        }
    }

    public void acceptRefund(Integer refundId, String token) {
        User seller = getUserByToken(token);

        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new BusinessException("Refund not found"));

        // Verify seller owns the post
        if (!refund.getReceiver().getId().equals(seller.getId())) {
            throw new BusinessException("You don't have permission to accept this refund");
        }

        if (!"REQUESTED".equalsIgnoreCase(refund.getRefundStatus())) {
            throw new BusinessException("Refund is not in requested status");
        }

        // Check if seller has bank account (for future reference)
        Bank sellerBank = bankRepository.findByUserId(seller.getId());
        if (sellerBank == null) {
            // Try alternative method as fallback
            sellerBank = bankRepository.findBankByUserId(seller.getId());
        }

        if (sellerBank == null) {
            throw new BusinessException("You must register a bank account before accepting refunds. Please go to your dashboard and register your bank account first.");
        }

        // Set status to APPROVED - seller will pay via VNPay to complete refund
        refund.setRefundStatus("APPROVED");
        refund.setUpdatedAt(Instant.now());
        refundRepository.save(refund);

        log.info("Refund {} accepted by seller {}. Payment required to complete refund.",
                refundId, seller.getId());
    }

    public String createRefundPaymentUrl(Integer refundId, String token, HttpServletRequest request) {
        User seller = getUserByToken(token);

        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new BusinessException("Refund not found"));

        // Verify seller owns the refund
        if (!refund.getReceiver().getId().equals(seller.getId())) {
            throw new BusinessException("You don't have permission to pay for this refund");
        }

        if (!"APPROVED".equalsIgnoreCase(refund.getRefundStatus())) {
            throw new BusinessException("Refund is not in approved status for payment");
        }

        Transaction transaction = refund.getTransaction();
        BigDecimal refundAmount = transaction.getPrice();

        log.info("Creating refund payment URL - RefundId: {}, Amount: {}, SellerId: {}",
                refundId, refundAmount, seller.getId());

        // Create payment URL using existing VNPay service
        // We'll create a special payment for refund (not regular transaction)
        try {
            String vnpayUrl = vnPayService.createRefundPaymentUrl(
                    refundAmount,
                    "Refund payment for order #" + refund.getId(),
                    request,
                    refundId
            );
            log.info("Successfully created refund payment URL for refund {}", refundId);
            return vnpayUrl;
        } catch (Exception e) {
            log.error("Failed to create refund payment URL for refund {}: {}", refundId, e.getMessage(), e);
            throw new BusinessException("Failed to create payment URL: " + e.getMessage());
        }
    }

    public void completeRefundPayment(Integer refundId, String token) {
        User seller = getUserByToken(token);

        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new BusinessException("Refund not found"));

        // Verify seller owns the refund
        if (!refund.getReceiver().getId().equals(seller.getId())) {
            throw new BusinessException("You don't have permission to complete this refund");
        }

        if (!"PAYMENT_PENDING".equalsIgnoreCase(refund.getRefundStatus())) {
            throw new BusinessException("Refund is not in payment pending status");
        }

        // Payment successful, now approve the refund
        refund.setRefundStatus("APPROVED");
        refund.setUpdatedAt(Instant.now());
        refundRepository.save(refund);

        log.info("Refund {} completed by seller {} after successful payment.",
                refundId, seller.getId());
    }

    public void rejectRefund(Integer refundId, String token) {
        User seller = getUserByToken(token);

        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new BusinessException("Refund not found"));

        // Verify seller owns the post
        if (!refund.getReceiver().getId().equals(seller.getId())) {
            throw new BusinessException("You don't have permission to reject this refund");
        }

        if (!"REQUESTED".equalsIgnoreCase(refund.getRefundStatus())) {
            throw new BusinessException("Refund is not in requested status");
        }

        // Update refund status
        refund.setRefundStatus("REJECTED");
        refund.setUpdatedAt(Instant.now());
        refundRepository.save(refund);

        log.info("Refund {} rejected by seller {}", refundId, seller.getId());
    }

    public String claimRefundMoney(Integer refundId, String token) {
        log.info("Starting claim refund money process for refund ID: {}", refundId);

        User buyer = getUserByToken(token);
        log.info("User claiming refund: ID={}, Email={}, FullName={}",
                buyer.getId(), buyer.getEmail(), buyer.getFullname());

        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new BusinessException("Refund not found"));

        log.info("Found refund: ID={}, Status={}, Sender={}, Receiver={}",
                refund.getId(), refund.getRefundStatus(), refund.getSender().getId(), refund.getReceiver().getId());

        // Verify buyer owns this refund
        if (!refund.getSender().getId().equals(buyer.getId())) {
            log.error("Permission denied: Buyer ID {} trying to claim refund owned by {}",
                    buyer.getId(), refund.getSender().getId());
            throw new BusinessException("You don't have permission to claim this refund");
        }

        if (!"APPROVED".equalsIgnoreCase(refund.getRefundStatus())) {
            log.error("Refund {} status is {} but expected APPROVED", refundId, refund.getRefundStatus());
            throw new BusinessException("Refund is not approved yet. Current status: " + refund.getRefundStatus());
        }

        Transaction transaction = refund.getTransaction();

        log.info("Processing refund money claim for refund {} with amount {}", refundId, transaction.getPrice());

        // Check if refund has already been claimed by checking claimedAt field
        if (refund.getClaimedAt() != null) {
            log.warn("Refund {} has already been claimed by buyer {} at {}", refundId, buyer.getId(), refund.getClaimedAt());
            return "This refund has already been claimed successfully.";
        }

        log.info("Processing refund claim - buyer: {}, transaction: {}", buyer.getId(), transaction.getTransactionid());

        try {
            // Add buyer to bank account (simulate refund to buyer's account)  
            Bank buyerBank = bankRepository.findBankByUserId(buyer.getId());
            if (buyerBank != null) {
                BigDecimal currentBalance = buyerBank.getBalance() != null ? buyerBank.getBalance() : BigDecimal.ZERO;
                buyerBank.setBalance(currentBalance.add(transaction.getPrice()));
                bankRepository.save(buyerBank);

                // Create bank transfer record for refund tracking
                Payment payment = paymentRepository.findPaymentByTransactionId(transaction.getTransactionid());
                log.info("Payment found for transaction {}: {}", transaction.getTransactionid(), payment != null);

                BankTransfer refundTransfer = BankTransfer.builder()
                        .payment(payment) // Can be null
                        .transaction(transaction)
                        .seller(refund.getReceiver()) // Original seller
                        .bank(buyerBank)
                        .amount(transaction.getPrice())
                        .transferStatus("COMPLETED")
                        .initiatedAt(Instant.now())
                        .completedAt(Instant.now())
                        .description("Refund claim for " + buyer.getFullname() + " - Transaction " + transaction.getTransactionid())
                        .build();
                bankTransferRepository.save(refundTransfer);

                // Update refund status to REFUNDED and set claimedAt timestamp
                refund.setRefundStatus("REFUNDED");
                refund.setClaimedAt(OffsetDateTime.now());
                refundRepository.save(refund);
                notificationService.sendForEvent(refund);

                // Update transaction status to FAILED to indicate it has been refunded
                transaction.setTransactionStatus("FAILED");
                transactionRepository.save(transaction);

                log.info("Refund {} claim processed successfully, refund status updated to REFUNDED, transaction status updated to FAILED, claimed at {}", refundId, refund.getClaimedAt());

                log.info("Refund {} processed successfully. Amount {} added to buyer {} bank account.",
                        refundId, transaction.getPrice(), buyer.getId());

                return "Refund processed successfully. Amount " + transaction.getPrice() + " VND has been credited to your bank account.";
            } else {
                // Update refund status to REFUNDED and set claimedAt timestamp even without bank account
                refund.setRefundStatus("REFUNDED");
                refund.setClaimedAt(OffsetDateTime.now());
                refundRepository.save(refund);

                // Update transaction status to FAILED to indicate it has been refunded
                transaction.setTransactionStatus("FAILED");
                transactionRepository.save(transaction);

                log.info("Refund {} processed successfully but no bank account found, refund and transaction status updated to REFUNDED/FAILED, claimed at {}", refund.getClaimedAt());

                log.info("Refund {} processed successfully for buyer {} but no bank account found.",
                        refundId, buyer.getId());

                return "Refund processed successfully. Please contact support to receive your refund amount of " + transaction.getPrice() + " VND.";
            }

        } catch (Exception e) {
            log.error("Error processing refund for refund {}: {}", refundId, e.getMessage(), e);
            throw new BusinessException("Failed to process refund: " + e.getMessage());
        }
    }

    // Admin methods
    public ResponseEntity<List<RefundResponse>> findAllBuyerRefundsByStatus(String status) {
        List<Refund> refunds;
        if ("ALL".equalsIgnoreCase(status)) {
            refunds = refundRepository.findAllBuyerRefunds();
        } else {
            refunds = refundRepository.findAllBuyerRefundsByStatus(status);
        }

        List<RefundResponse> responses = refunds.stream()
                .map(refundMapper::toRefundResponse)
                .toList();

        return ResponseEntity.ok(responses);
    }

    public ResponseEntity<List<RefundResponse>> findAllSellerRefundsByStatus(String status) {
        List<Refund> refunds;
        if ("ALL".equalsIgnoreCase(status)) {
            refunds = refundRepository.findAllSellerRefunds();
        } else {
            refunds = refundRepository.findAllSellerRefundsByStatus(status);
        }

        List<RefundResponse> responses = refunds.stream()
                .map(refundMapper::toRefundResponse)
                .toList();

        return ResponseEntity.ok(responses);
    }

    public ResponseEntity<List<RefundResponse>> findAllRefundsByStatus(String status) {
        List<Refund> refunds;
        if ("ALL".equalsIgnoreCase(status)) {
            refunds = refundRepository.findAll();
        } else {
            refunds = refundRepository.findByRefundStatus(status);
        }

        List<RefundResponse> responses = refunds.stream()
                .map(refundMapper::toRefundResponse)
                .toList();

        return ResponseEntity.ok(responses);
    }

    public void adminAcceptRefund(Integer refundId, String token) {
        User admin = getUserByToken(token);

        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new BusinessException("Refund not found"));

        if (!"REQUESTED".equalsIgnoreCase(refund.getRefundStatus())) {
            throw new BusinessException("Refund is not in requested status");
        }

        // Get seller bank account
        Bank sellerBank = bankRepository.findByUserId(refund.getReceiver().getId());
        if (sellerBank == null) {
            throw new BusinessException("Seller bank account not found");
        }

        // Check if seller has enough balance
        Transaction transaction = refund.getTransaction();
        BigDecimal refundAmount = transaction.getPrice();

        if (sellerBank.getBalance().compareTo(refundAmount) < 0) {
            throw new BusinessException("Seller doesn't have enough balance for refund");
        }

        // Deduct money from seller
        sellerBank.setBalance(sellerBank.getBalance().subtract(refundAmount));
        bankRepository.save(sellerBank);

        // Update refund status
        refund.setRefundStatus("APPROVED");
        refund.setUpdatedAt(Instant.now());
        refundRepository.save(refund);

        log.info("Refund {} accepted by admin {}. Amount {} deducted from seller {}",
                refundId, admin.getId(), refundAmount, refund.getReceiver().getId());
    }

    public void adminRejectRefund(Integer refundId, String token) {
        User admin = getUserByToken(token);

        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new BusinessException("Refund not found"));

        if (!"REQUESTED".equalsIgnoreCase(refund.getRefundStatus())) {
            throw new BusinessException("Refund is not in requested status");
        }

        // Update refund status
        refund.setRefundStatus("REJECTED");
        refund.setUpdatedAt(Instant.now());
        refundRepository.save(refund);

        log.info("Refund {} rejected by admin {}", refundId, admin.getId());
    }
}