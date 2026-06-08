package com.g_wuy.swp391.voltera.service;

import com.g_wuy.swp391.voltera.entity.*;
import com.g_wuy.swp391.voltera.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class NotificationService {

    NotificationRepository notificationRepository;

    public void sendForEvent(Object event) {
        if (event instanceof Payment payment) {
            handlePaymentNotification(payment);
        } else if (event instanceof Contract contract) {
            handleContractNotification(contract);
        } else if (event instanceof Transaction transaction) {
            handleTransactionNotification(transaction);
        } else if (event instanceof Post post) {
            handlePostNotification(post);
        } else if (event instanceof Refund refund) {
            handleRefundNotification(refund);
        }
    }

    // ====== PAYMENT ======
    private void handlePaymentNotification(Payment payment) {
        Transaction transaction = payment.getTransaction();
        if (transaction == null || transaction.getBuyerid() == null) return;

        String status = payment.getPaymentStatus();
        String title = "Payment Update";
        String message;

        switch (status.toUpperCase()) {
            case "COMPLETED" ->
                    message = "Your payment for post #" + transaction.getPost().getId() + " was successful.";
            case "FAILED" ->
                    message = "Your payment for post #" + transaction.getPost().getId() + " has failed. Please try again.";
            case "PENDING" ->
                    message = "Your payment for post #" + transaction.getPost().getId() + " is being processed.";
            default -> message = "Unknown payment status.";
        }

        save(transaction.getBuyerid(), title, message);
    }

    // ====== CONTRACT ======
    private void handleContractNotification(Contract contract) {
        String status = contract.getContractstatus();
        String title = "Contract Update";
        String message;

        boolean buyerSigned = Boolean.TRUE.equals(contract.getBuyersigned());
        boolean sellerSigned = Boolean.TRUE.equals(contract.getSellersigned());


        if ("PENDING".equalsIgnoreCase(status) && buyerSigned && !sellerSigned) {
            message = "A new contract #" + contract.getId() +
                    " has been created and signed by the buyer. Please review and sign it.";
            if (contract.getSellerid() != null)
                save(contract.getSellerid(), title, message);
            return;
        }

        switch (status.toUpperCase()) {
            case "SIGNED" -> message = "Contract #" + contract.getId() + " has been signed by both parties.";
            case "CANCELLED" -> message = "Contract #" + contract.getId() + " has been cancelled.";
            case "PENDING" -> message = "Contract #" + contract.getId() + " is pending approval.";
            default -> message = "Unknown contract status.";
        }

        if (contract.getBuyerid() != null)
            save(contract.getBuyerid(), title, message);
        if (contract.getSellerid() != null)
            save(contract.getSellerid(), title, message);
    }

    // ====== TRANSACTION ======
    private void handleTransactionNotification(Transaction transaction) {
        String status = transaction.getTransactionStatus();
        String title = "Transaction Update";
        String message;

        switch (status.toUpperCase()) {
            case "DONE" ->
                    message = "Transaction #" + transaction.getTransactionid() + " has been completed successfully.";
            case "FAILED" -> message = "Transaction #" + transaction.getTransactionid() + " has failed.";
            case "PENDING" ->
                    message = "Transaction #" + transaction.getTransactionid() + " is currently being processed.";
            default -> message = "Unknown transaction status.";
        }

        if (transaction.getBuyerid() != null)
            save(transaction.getBuyerid(), title, message);


        if (transaction.getPost() != null && transaction.getPost().getSellerId() != null)
            save(transaction.getPost().getSellerId(), title, message);

    }

    private void handlePostNotification(Post post) {
        if (post.getSellerId() == null) return;

        String title = "Post Update";
        String message;

        switch (post.getStatus().toUpperCase()) {
            case "APPROVE" -> message = "Your post \"" + post.getTitle() + "\" has been approved.";
            case "REJECT" ->
                    message = "Your post \"" + post.getTitle() + "\" has been rejected. Please review the admin feedback.";
            default -> message = "Your post \"" + post.getTitle() + "\" has been updated.";
        }

        save(post.getSellerId(), title, message);
    }

    private void handleRefundNotification(Refund refund) {
        if (refund == null) return;

        String title = "Refund Update";
        String message = "Your refund for transaction #" + refund.getTransaction().getTransactionid()
                + " is now " + refund.getRefundStatus();

        if (refund.getSender() != null)
            save(refund.getSender(), title, message);

        if (refund.getReceiver() != null)
            save(refund.getReceiver(), title, message);
    }

    private void save(User user, String title, String message) {
        if (user == null) return;

        Notification notification = Notification.builder()
                .userid(user)
                .title(title)
                .message(message)
                .createdAt(Instant.now())
                .readStatus(false)
                .build();

        notificationRepository.save(notification);
    }
}
