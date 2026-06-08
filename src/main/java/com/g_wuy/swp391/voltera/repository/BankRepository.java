package com.g_wuy.swp391.voltera.repository;

import com.g_wuy.swp391.voltera.entity.Bank;
import com.g_wuy.swp391.voltera.model.response.BankResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankRepository extends JpaRepository<Bank, Integer> {

    Bank findByUserId(Integer userId);

    boolean existsByBankNumber(String bankNumber);

    @Query("SELECT new com.g_wuy.swp391.voltera.model.response.BankResponse(b.accountName, b.bankName, b.bankNumber, b.balance, b.securityCode, b.status, b.expDate) " +
            "FROM Bank b where b.status = :status")
    List<BankResponse> findAllByStatus(String status);


    @Query("SELECT new com.g_wuy.swp391.voltera.model.response.BankResponse(b.accountName, b.bankName, b.bankNumber, b.balance, b.securityCode, b.status, b.expDate) " +
            "FROM Bank b")
    List<BankResponse> getAllBank();

    Bank findBankByUserId(Integer userId);
}