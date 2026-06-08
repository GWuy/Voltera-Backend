package com.g_wuy.swp391.voltera.repository;

import com.g_wuy.swp391.voltera.entity.BankTransfer;
import com.g_wuy.swp391.voltera.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankTransferRepository extends JpaRepository<BankTransfer, Integer> {
    List<BankTransfer> findByTransaction(Transaction transaction);

    List<BankTransfer> findByTransactionAndDescriptionContaining(Transaction transaction, String description);
}
