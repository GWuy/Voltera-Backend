package com.g_wuy.swp391.voltera.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.g_wuy.swp391.voltera.model.response.ApproveResponse;
import com.g_wuy.swp391.voltera.model.response.ProfileResponse;

import com.g_wuy.swp391.voltera.entity.Account;
import com.g_wuy.swp391.voltera.entity.User;
import com.g_wuy.swp391.voltera.exception.BusinessException;
import com.g_wuy.swp391.voltera.mapper.AccountMapper;
import com.g_wuy.swp391.voltera.mapper.UserMapper;
import com.g_wuy.swp391.voltera.model.request.ProfileRequest;
import com.g_wuy.swp391.voltera.service.AccountService;
import com.g_wuy.swp391.voltera.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {

    AccountService accountService;

    UserService userService;

    UserMapper userMapper;

    AccountMapper accountMapper;

    @PutMapping("/api/v1/admin/account/{id}/approved")
    public ResponseEntity<ApproveResponse> approve(@PathVariable Integer id) {
        Account accountApprove = accountService.approveAccount(id);
        return ResponseEntity.ok(accountMapper.toAccountResponse(accountApprove));
    }

    @PutMapping("/api/v1/admin/account/{id}/rejected")
    public ResponseEntity<ApproveResponse> reject(@PathVariable Integer id) {
        Account accountApprove = accountService.rejectAccount(id);
        return ResponseEntity.ok(accountMapper.toAccountResponse(accountApprove));
    }

    @GetMapping("/api/v1/users/me/profile")
    public ResponseEntity<ProfileResponse> getUserProfile(Authentication authentication) {
        String username = authentication.getName();
        User user = userService.getUserProfile(username);
        return ResponseEntity.ok(userMapper.toProfileResponse(user));
    }

    @PutMapping("/api/v1/users/me/profile")
    public ResponseEntity<ProfileResponse> saveProfile(
            Authentication authentication,
            @RequestBody ProfileRequest request) {

        String username = authentication.getName();
        // Get current user's ID from authentication
        User currentUser = userService.findUserByUsername(username);
        if (currentUser == null) {
            throw new BusinessException("User not found");
        }

        User userProfile = userService.saveProfile(currentUser.getId(), request);
        return ResponseEntity.ok(userMapper.toProfileResponse(userProfile));
    }

    @GetMapping("/me")
    public Map<String, Object> getCurrentUser(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        response.put("username", authentication.getName());
        response.put("roles", authentication.getAuthorities());
        return response;
    }

    @GetMapping("/api/v1/admin/accounts/pending")
    public ResponseEntity<List<ApproveResponse>> getPendingAccounts() {
        System.out.println("[DEBUG] Getting pending accounts...");
        List<Account> pendingAccounts = accountService.getPendingAccounts();
        System.out.println("[DEBUG] Found " + pendingAccounts.size() + " pending accounts");

        for (Account acc : pendingAccounts) {
            System.out.println("[DEBUG] Account: " + acc.getUsername() + " | Status: " + acc.getStatus());
        }

        List<ApproveResponse> response = pendingAccounts.stream()
                .map(accountMapper::toAccountResponse)
                .collect(Collectors.toList());
        System.out.println("[DEBUG] Returning " + response.size() + " responses");
        return ResponseEntity.ok(response);
    }

    // Debug endpoint to check all accounts
    @GetMapping("/api/v1/admin/accounts/all")
    public ResponseEntity<List<ApproveResponse>> getAllAccounts() {
        System.out.println("Admin requesting ALL accounts for debug...");
        List<Account> allAccounts = accountService.getAllAccounts();
        System.out.println("Found " + allAccounts.size() + " total accounts");

        for (Account acc : allAccounts) {
            System.out.println("Account: " + acc.getUsername() + " | Status: " + acc.getStatus() + " | Role: " + acc.getRole());
        }

        List<ApproveResponse> response = allAccounts.stream()
                .map(accountMapper::toAccountResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/v1/admin/accounts/approved")
    public ResponseEntity<List<ApproveResponse>> getApprovedAccounts() {
        System.out.println("[DEBUG] Getting approved accounts...");
        System.out.println("[DEBUG] User requesting: " + SecurityContextHolder.getContext().getAuthentication().getName());
        System.out.println("[DEBUG] User roles: " + SecurityContextHolder.getContext().getAuthentication().getAuthorities());

        List<Account> approvedAccounts = accountService.getApprovedAccounts();
        System.out.println("[DEBUG] Found " + approvedAccounts.size() + " approved accounts");

        for (Account acc : approvedAccounts) {
            System.out.println("[DEBUG] Account: " + acc.getUsername() + " | Status: " + acc.getStatus() + " | Role: " + acc.getRole());
        }

        List<ApproveResponse> response = approvedAccounts.stream()
                .map(accountMapper::toAccountResponse)
                .collect(Collectors.toList());
        System.out.println("[DEBUG] Returning " + response.size() + " responses");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/api/v1/admin/account/{id}/lock")
    public ResponseEntity<ApproveResponse> lockAccount(
            @PathVariable Integer id,
            @RequestHeader("Authorization") String token) {
        try {
            System.out.println("[DEBUG] Lock request by user: " + SecurityContextHolder.getContext().getAuthentication().getName());
            userService.lockAccount(id, token);
            Account lockedAccount = accountService.findAccountById(id);
            ApproveResponse response = accountMapper.toAccountResponse(lockedAccount);
            System.out.println("[DEBUG] Account locked: " + lockedAccount.getUsername() + " | New Status: " + lockedAccount.getStatus());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to lock account " + id + ": " + e.getMessage());
            throw e;
        }
    }

    @PutMapping("/api/v1/admin/account/{id}/unlock")
    public ResponseEntity<ApproveResponse> unlockAccount(
            @PathVariable Integer id,
            @RequestHeader("Authorization") String token) {
        try {
            System.out.println("[DEBUG] Unlock request by user: " + SecurityContextHolder.getContext().getAuthentication().getName());
            userService.unlockAccount(id, token);
            Account unlockedAccount = accountService.findAccountById(id);
            ApproveResponse response = accountMapper.toAccountResponse(unlockedAccount);
            System.out.println("[DEBUG] Account unlocked: " + unlockedAccount.getUsername() + " | New Status: " + unlockedAccount.getStatus());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to unlock account " + id + ": " + e.getMessage());
            throw e;
        }
    }

}
