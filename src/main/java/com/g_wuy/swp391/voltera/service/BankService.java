package com.g_wuy.swp391.voltera.service;

import com.g_wuy.swp391.voltera.entity.Bank;
import com.g_wuy.swp391.voltera.entity.User;
import com.g_wuy.swp391.voltera.exception.BusinessException;
import com.g_wuy.swp391.voltera.mapper.BankMapper;
import com.g_wuy.swp391.voltera.model.dto.BankRegistrationDTO;
import com.g_wuy.swp391.voltera.model.request.BankRequest;
import com.g_wuy.swp391.voltera.model.response.BankResponse;
import com.g_wuy.swp391.voltera.repository.BankRepository;
import com.g_wuy.swp391.voltera.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestHeader;

import javax.security.auth.login.LoginException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.chrono.ChronoLocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class BankService {

    BankRepository bankRepository;

    JwtService jwtService;

    UserRepository userRepository;

    BankMapper bankMapper;

    public ResponseEntity<BankResponse> saveBank(BankRequest request, @RequestHeader("Authorization") String jwt) {
        String token = jwt.substring(7);
        User user = userRepository.findUserByUsername(jwtService.extractUsername(token));
        Bank bank = new Bank();

        if (user == null) {
            throw new BusinessException("User not found");
        }
        bank.setUser(user);
        bank.setBankName(request.getBankName());
        int len = request.getBankNumber().length();
        if (len < 12 || len > 19) {
            throw new BusinessException("Bank number must be between 12 and 19 digits");
        }
        if (bankRepository.existsByBankNumber(request.getBankNumber())) {
            throw new BusinessException("Bank number already exists");
        }
        bank.setBankNumber(request.getBankNumber());
        bank.setAccountName(request.getAccountName());
        bank.setSecurityCode(request.getSecurityCode());
        if (request.getExpirationDate().isBefore(java.time.LocalDate.now())) {
            throw new BusinessException("Expiration date is invalid");
        }
        bank.setExpDate(request.getExpirationDate());
        bank.setUpdatedAt(Instant.now());
        bank.setBalance(BigDecimal.valueOf(0.0));
        bank.setStatus("ACTIVE");
        bankRepository.save(bank);
        return ResponseEntity.ok(bankMapper.toBankResponse(bank));
    }

    public ResponseEntity<BankResponse> myBanks(@RequestHeader("Authorization") String jwt) {
        String token = jwt.substring(7);
        User user = userRepository.findUserByUsername(jwtService.extractUsername(token));
        Bank bank = bankRepository.findByUserId(user.getId());
        return ResponseEntity.ok(bankMapper.toBankResponse(bank));
    }

    public ResponseEntity<List<BankResponse>> getAllBank(String status) {
        List<BankResponse> bankResponses;
        if ("".equalsIgnoreCase(status) || status == null) {
            bankResponses = bankRepository.getAllBank();
        } else {
            bankResponses = bankRepository.findAllByStatus(status);
        }
        return ResponseEntity.ok(bankResponses);
    }

    public boolean existsByUserId(Integer userId) {
        return bankRepository.findByUserId(userId) != null;
    }

    public Bank findByUserId(Integer userId) {
        return bankRepository.findByUserId(userId);
    }

    public Bank createSellerBankAccount(User user, BankRegistrationDTO bankDTO) {
        Bank bank = new Bank();
        bank.setUser(user);
        bank.setBankName(bankDTO.getBankName());
        bank.setBankNumber(bankDTO.getBankNumber());
        bank.setAccountName(bankDTO.getAccountName());
        bank.setSecurityCode(Integer.parseInt(bankDTO.getSecurityCode()));
        bank.setExpDate(bankDTO.getExpDate());
        bank.setBalance(BigDecimal.valueOf(0.0));
        bank.setStatus("ACTIVE");
        bank.setCreatedAt(Instant.now());
        bank.setUpdatedAt(Instant.now());

        return bankRepository.save(bank);
    }

    public void depositMoney(BigDecimal amount, String token) {
        String jwtToken = token.substring(7);
        String username = jwtService.extractUsername(jwtToken);
        User user = userRepository.findUserByUsername(username);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        Bank bank = bankRepository.findByUserId(user.getId());
        if (bank == null) {
            throw new RuntimeException("Bank account not found. Please register your bank account first.");
        }

        // Add money to current balance
        BigDecimal currentBalance = bank.getBalance() != null ? bank.getBalance() : BigDecimal.ZERO;
        bank.setBalance(currentBalance.add(amount));
        bank.setUpdatedAt(Instant.now());

        bankRepository.save(bank);
    }

    public BigDecimal getBalance(String token) {
        String jwtToken = token.substring(7);
        String username = jwtService.extractUsername(jwtToken);
        User user = userRepository.findUserByUsername(username);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        Bank bank = bankRepository.findByUserId(user.getId());
        if (bank == null) {
            return BigDecimal.ZERO; // Return 0 if no bank account
        }

        return bank.getBalance() != null ? bank.getBalance() : BigDecimal.ZERO;
    }
}