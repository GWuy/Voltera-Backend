package com.g_wuy.swp391.voltera.controller;

import com.g_wuy.swp391.voltera.model.response.ProductInformationTransactionResponse;
import com.g_wuy.swp391.voltera.model.response.TransactionResponse;
import com.g_wuy.swp391.voltera.service.TransactionService;

import java.util.List;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TransactionController {

    TransactionService transactionService;

    @GetMapping("/by-status/{transactionStatus}")
    public ResponseEntity<List<TransactionResponse>> findTransactionsByUser(
            @RequestHeader("Authorization") String token,
            @PathVariable String transactionStatus) {
        return ResponseEntity.ok(transactionService.getTransactionByStatus(transactionStatus, token).getBody());
    }

    @GetMapping("/detail/{transactionId}")
    public ResponseEntity<TransactionResponse> getTransactionDetail(
            @RequestHeader("Authorization") String token,
            @PathVariable Integer transactionId) {
        return transactionService.getTransactionDetail(transactionId, token);
    }

    @GetMapping("/status/{transactionId}")
    public ResponseEntity<com.g_wuy.swp391.voltera.model.response.TransactionStatusResponse> getTransactionStatus(
            @PathVariable Integer transactionId) {
        return transactionService.getTransactionStatus(transactionId);
    }

    @GetMapping("/product-info/{transactionId}")
    public ResponseEntity<ProductInformationTransactionResponse> getProductInfo(
            @PathVariable Integer transactionId) {
        return transactionService.getProductInformationByTransactionId(transactionId);
    }
}
