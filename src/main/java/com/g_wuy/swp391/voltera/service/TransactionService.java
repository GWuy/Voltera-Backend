package com.g_wuy.swp391.voltera.service;

import com.g_wuy.swp391.voltera.entity.Contract;
import com.g_wuy.swp391.voltera.entity.Transaction;
import com.g_wuy.swp391.voltera.entity.User;
import com.g_wuy.swp391.voltera.mapper.TransactionMapper;
import com.g_wuy.swp391.voltera.model.response.TransactionResponse;
import com.g_wuy.swp391.voltera.repository.TransactionRepository;
import com.g_wuy.swp391.voltera.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class TransactionService {

    TransactionRepository transactionRepository;

    TransactionMapper transactionMapper;

    JwtService jwtService;

    UserRepository userRepository;

    public ResponseEntity<List<TransactionResponse>> getTransactionByStatus(String status, String token) {
        String jwt = token.substring(7);
        String username = jwtService.extractUsername(jwt);
        User user = userRepository.findUserByUsername(username);

        List<TransactionResponse> transactionResponseList;

        if (status == null || status.isEmpty()) {
            // Nếu không truyền status -> lấy tất cả giao dịch của user
            List<Transaction> transactions = transactionRepository.findTransactionsByUser(username);
            transactionResponseList = transactions.stream()
                    .map(transactionMapper::toResponse)
                    .toList();
        } else {
            // Nếu có status -> lấy theo userId + status
            transactionResponseList = transactionRepository.findTransactionByStatus(user.getId(), status);
        }

        return ResponseEntity.ok(transactionResponseList);
    }

    public ResponseEntity<TransactionResponse> getTransactionDetail(Integer transactionId, String token) {
        String jwt = token.substring(7);
        String username = jwtService.extractUsername(jwt);
        User user = userRepository.findUserByUsername(username);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        // Kiểm tra quyền truy cập đơn giản hơn
        boolean hasAccess = false;

        // Kiểm tra qua contract
        if (transaction.getContractid() != null) {
            Contract contract = transaction.getContractid();
            if (contract.getBuyerid() != null && contract.getBuyerid().getId().equals(user.getId())) {
                hasAccess = true;
            }
            if (contract.getSellerid() != null && contract.getSellerid().getId().equals(user.getId())) {
                hasAccess = true;
            }
        }

        // Kiểm tra qua buyer trực tiếp
        if (transaction.getBuyerid() != null && transaction.getBuyerid().getId().equals(user.getId())) {
            hasAccess = true;
        }

        // Kiểm tra qua post seller
        if (transaction.getPost() != null && transaction.getPost().getSellerId() != null &&
                transaction.getPost().getSellerId().getId().equals(user.getId())) {
            hasAccess = true;
        }

        if (!hasAccess) {
            throw new RuntimeException("Access denied to this transaction");
        }

        TransactionResponse response = transactionMapper.toResponse(transaction);
        return ResponseEntity.ok(response);
    }

}
