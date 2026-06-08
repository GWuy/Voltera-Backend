package com.g_wuy.swp391.voltera.controller;

import com.g_wuy.swp391.voltera.entity.Bank;
import com.g_wuy.swp391.voltera.entity.User;
import com.g_wuy.swp391.voltera.model.dto.BankRegistrationDTO;
import com.g_wuy.swp391.voltera.model.request.BankRequest;
import com.g_wuy.swp391.voltera.model.response.BankResponse;
import com.g_wuy.swp391.voltera.service.BankService;
import com.g_wuy.swp391.voltera.service.JwtService;
import com.g_wuy.swp391.voltera.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bank")
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BankController {

    BankService bankService;

    UserService userService;

    JwtService jwtService;

    @GetMapping("/{status}")
    private ResponseEntity<List<BankResponse>> findAllByStatus(
            @PathVariable String status) {
        return ResponseEntity.ok(bankService.getAllBank(status).getBody());
    }

    @PostMapping("/save-bank")
    private ResponseEntity<BankResponse> saveBank(
            @RequestHeader("Authorization") String jwt,
            @RequestBody BankRequest request) {
        return ResponseEntity.ok(bankService.saveBank(request, jwt).getBody());
    }

    @GetMapping("/my-bank")
    private ResponseEntity<BankResponse> getBank(@RequestHeader("Authorization") String jwt) {
        return ResponseEntity.ok(bankService.myBanks(jwt).getBody());
    }

    @PostMapping("/register-seller")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<?> registerSellerBankAccount(
            @Valid @RequestBody BankRegistrationDTO bankDTO,
            HttpServletRequest request) {
        try {
            // Validate expiration date
            if (!bankDTO.isExpDateValid()) {
                return ResponseEntity.badRequest()
                        .body("Expiration date must be in the future");
            }

            // Get user from JWT token
            String authHeader = request.getHeader("Authorization");
            String token = authHeader.substring(7);
            String username = jwtService.extractUsername(token);
            User user = userService.findByUsername(username);

            // Check if user already has bank account
            if (bankService.existsByUserId(user.getId())) {
                return ResponseEntity.badRequest()
                        .body("Bank account already registered for this user");
            }

            Bank bank = bankService.createSellerBankAccount(user, bankDTO);

            return ResponseEntity.ok()
                    .body("Bank account registered successfully for seller");

        } catch (Exception e) {
            log.error("Error registering seller bank account", e);
            return ResponseEntity.internalServerError()
                    .body("Failed to register bank account: " + e.getMessage());
        }
    }

    @PostMapping("/deposit")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<?> depositMoney(
            @RequestParam BigDecimal amount,
            @RequestHeader("Authorization") String token) {
        try {
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest()
                        .body("Deposit amount must be greater than 0");
            }

            if (amount.compareTo(BigDecimal.valueOf(100000000)) > 0) { // Max 100M VND
                return ResponseEntity.badRequest()
                        .body("Deposit amount cannot exceed 100,000,000 VND");
            }

            bankService.depositMoney(amount, token);
            return ResponseEntity.ok("Money deposited successfully");

        } catch (Exception e) {
            log.error("Error depositing money", e);
            return ResponseEntity.internalServerError()
                    .body("Failed to deposit money: " + e.getMessage());
        }
    }

    @GetMapping("/balance")
    @PreAuthorize("hasAnyRole('SELLER', 'BUYER')")
    public ResponseEntity<?> getBalance(@RequestHeader("Authorization") String token) {
        try {
            BigDecimal balance = bankService.getBalance(token);
            return ResponseEntity.ok(Map.of("balance", balance));
        } catch (Exception e) {
            log.error("Error getting balance", e);
            return ResponseEntity.internalServerError()
                    .body("Failed to get balance: " + e.getMessage());
        }
    }
}
