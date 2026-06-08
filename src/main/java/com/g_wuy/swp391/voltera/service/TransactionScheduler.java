package com.g_wuy.swp391.voltera.service;

import com.g_wuy.swp391.voltera.entity.Transaction;
import com.g_wuy.swp391.voltera.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class TransactionScheduler {

    TransactionRepository transactionRepository;


    NotificationService notificationService;

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void cancelExpiredTransactions() {
        Instant fifteenDaysAgo = Instant.now().minus(15, ChronoUnit.DAYS);

        List<Transaction> expiredTransactions = transactionRepository.findAll().stream()
                .filter(t -> "PENDING".equalsIgnoreCase(t.getTransactionStatus()))
                .filter(t -> t.getCreateAt().isBefore(fifteenDaysAgo))
                .toList();

        for (Transaction t : expiredTransactions) {
            t.setTransactionStatus("FAILED");
            transactionRepository.save(t);
            notificationService.sendForEvent(t);
        }
    }
}