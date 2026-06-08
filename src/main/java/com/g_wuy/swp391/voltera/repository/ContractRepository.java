package com.g_wuy.swp391.voltera.repository;

import com.g_wuy.swp391.voltera.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Integer> {
    @Query("""
                SELECT c FROM Contract c
                WHERE 
                    (c.buyerid.id IN (SELECT a.user.id FROM Account a WHERE a.username = :username)
                     OR c.sellerid.id IN (SELECT a.user.id FROM Account a WHERE a.username = :username))
                    AND c.contractstatus <> 'CANCELLED'
            """)
    List<Contract> findActiveContractsByUser(@Param("username") String username);

    List<Contract> findByContractstatusAndExpirationdateBefore(String status, LocalDate date);
}