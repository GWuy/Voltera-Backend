package com.g_wuy.swp391.voltera.repository;

import com.g_wuy.swp391.voltera.entity.User;
import jakarta.transaction.Transactional;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.g_wuy.swp391.voltera.entity.Account;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {
    Optional<Account> findByUsername(String username);

    Optional<Account> findByUser(User user);

    Account findAccountById(Integer id);

    @Query("SELECT COUNT(a) > 0 FROM Account a WHERE a.username = :username")
    boolean existsAccountByUsername(@Param("username") String username);

    @Modifying
    @Transactional
    @Query("Update Account a SET a.status = 'APPROVE' WHERE a.id = :id")
    int approveAccountById(@Param("id") Integer id);

    @Query("SELECT COUNT(a) > 0 FROM Account a WHERE a.username = :username")
    boolean isEmailExist(@Param("username") String username);

    @Query("SELECT a FROM Account a WHERE a.status = 'PENDING' ORDER BY a.createat DESC")
    List<Account> findPendingAccounts();

    @Query("SELECT a FROM Account a ORDER BY a.createat DESC")
    List<Account> findAllAccountsOrdered();

    @Query("SELECT a FROM Account a WHERE (a.status = 'APPROVE' OR a.status = 'INACTIVE') AND a.role != 'ADMIN' ORDER BY a.createat DESC")
    List<Account> findApprovedAccounts();

    @Modifying
    @Transactional
    @Query("Update Account a SET a.status = 'REJECT' WHERE a.id = :id")
    int rejectAccountById(@Param("id") Integer id);
}
