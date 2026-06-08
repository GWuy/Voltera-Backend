package com.g_wuy.swp391.voltera.service;

import com.g_wuy.swp391.voltera.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.g_wuy.swp391.voltera.entity.Account;
import com.g_wuy.swp391.voltera.repository.AccountRepository;

@Service
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class UserDetailImplementService implements UserDetailsService {

    AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        return accountRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("Account not found"));
    }
}
