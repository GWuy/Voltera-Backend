package com.g_wuy.swp391.voltera.repository;

import com.g_wuy.swp391.voltera.entity.Transaction;
import com.g_wuy.swp391.voltera.model.response.TransactionResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

    @Query("""
                SELECT COALESCE(SUM(t.price), 0)
                FROM Transaction t
                WHERE t.transactionStatus = 'DONE'
                  AND t.createAt BETWEEN :start AND :end
            """)
    BigDecimal getTotalRevenueInPeriod(@Param("start") Instant start, @Param("end") Instant end);

    @Query("""
                SELECT COUNT(t)
                FROM Transaction t
                WHERE t.transactionStatus = 'DONE'
                  AND t.createAt BETWEEN :start AND :end
            """)
    Long getTotalTransactionsInPeriod(@Param("start") Instant start, @Param("end") Instant end);

    @Query("""
                SELECT t FROM Transaction t
                WHERE 
                    (t.contractid.buyerid.id IN (SELECT a.user.id FROM Account a WHERE a.username = :username)
                     OR t.contractid.sellerid.id IN (SELECT a.user.id FROM Account a WHERE a.username = :username))
                    AND t.transactionStatus IN ('PENDING', 'FAIL', 'DONE')
            """)
    List<Transaction> findTransactionsByUser(@Param("username") String username);

    @Query("""
                SELECT new com.g_wuy.swp391.voltera.model.response.TransactionResponse(
                    t.transactionid,
                    p.id,
                    p.title,
                    p.price,
                    t.transactionStatus,
                    t.createAt,
                    t.updateAt
                )
                FROM Transaction t
                JOIN t.post p
                JOIN p.sellerId s
                LEFT JOIN t.buyerid b
                WHERE (s.id = :userId OR b.id = :userId)
                  AND t.transactionStatus = :status
            """)
    List<TransactionResponse> findTransactionByStatus(
            @Param("userId") Integer userId,
            @Param("status") String status
    );

    @Query("SELECT t FROM Transaction t WHERE t.post.id = :postId")
    Transaction findByPostId(@Param("postId") Integer postId);
}