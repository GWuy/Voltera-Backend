package com.g_wuy.swp391.voltera.service;

import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.g_wuy.swp391.voltera.configuration.SecurityConfig;
import com.g_wuy.swp391.voltera.entity.Account;
import com.g_wuy.swp391.voltera.entity.User;
import com.g_wuy.swp391.voltera.exception.BusinessException;
import com.g_wuy.swp391.voltera.mapper.AccountMapper;
import com.g_wuy.swp391.voltera.model.request.RegisterRequest;
import com.g_wuy.swp391.voltera.model.response.RegisterResponse;
import com.g_wuy.swp391.voltera.repository.AccountRepository;
import com.g_wuy.swp391.voltera.repository.UserRepository;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AccountService {

    AccountRepository accountRepository;

    AccountMapper accountMapper;

    UserRepository userRepository;

    SecurityConfig securityConfiguration;

    public Account save(Account account) {
        return accountRepository.save(account);
    }

    public RegisterResponse register(RegisterRequest registerRequest) {
        String username = registerRequest.getUsername();
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";

        if (accountRepository.existsAccountByUsername(username)) {
            throw new BusinessException("This username already existed");
        }

        if (username.matches(emailRegex)) {
            if (accountRepository.isEmailExist(username)) {
                throw new BusinessException("This email already exists");
            }
        }

        String email = username.matches(emailRegex) ? username : null;
        User userRegis = new User();
        userRegis.setEmail(email);
        userRegis.setEmailVerified(true);
        userRegis.setIsUpdatedProfile(false);
        User userSaved = userRepository.save(userRegis);

        Account account = accountMapper.toAccount(registerRequest);
        account.setUser(userSaved);
        account.setStatus("APPROVE");
        account.setRole(registerRequest.getRole());
        account.setPassword(securityConfiguration.passwordEncoder().encode(registerRequest.getPassword()));

        accountRepository.save(account);
        // Không tự động gửi OTP nữa vì đã verify trước đó

        return accountMapper.toRegisterResponse(account);
    }

    @Transactional
    public Account approveAccount(Integer id) {
        int approved = accountRepository.approveAccountById(id);
        if (approved == 0) {
            throw new BusinessException("Account isn't exist or already approved");
        }
        return accountRepository.findAccountById(id);
    }

    @Transactional
    public Account rejectAccount(Integer id) {
        int rejected = accountRepository.rejectAccountById(id);
        if (rejected == 0) {
            throw new BusinessException("Account isn't exist or already rejected");
        }
        return accountRepository.findAccountById(id);
    }

    public Account findAccountById(Integer id) {
        return accountRepository.findAccountById(id);
    }

    public Optional<Account> findAccountByUsername(String username) {
        if (username == null || username.isEmpty()) {
            throw new BusinessException("Username not found");
        }
        return accountRepository.findByUsername(username);
    }

    public List<Account> getPendingAccounts() {
        return accountRepository.findPendingAccounts();
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAllAccountsOrdered();
    }

    public List<Account> getApprovedAccounts() {
        return accountRepository.findApprovedAccounts();
    }

}