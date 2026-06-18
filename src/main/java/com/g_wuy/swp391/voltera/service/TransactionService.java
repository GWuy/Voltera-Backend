package com.g_wuy.swp391.voltera.service;

import com.g_wuy.swp391.voltera.entity.Contract;
import com.g_wuy.swp391.voltera.entity.Post;
import com.g_wuy.swp391.voltera.entity.Transaction;
import com.g_wuy.swp391.voltera.entity.User;
import com.g_wuy.swp391.voltera.mapper.TransactionMapper;
import com.g_wuy.swp391.voltera.model.response.ProductInformationTransactionResponse;
import com.g_wuy.swp391.voltera.model.response.TransactionResponse;
import com.g_wuy.swp391.voltera.model.response.TransactionStatusResponse;
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

    public ResponseEntity<TransactionStatusResponse> getTransactionStatus(Integer transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));
        return ResponseEntity.ok(
            com.g_wuy.swp391.voltera.model.response.TransactionStatusResponse.builder()
                .transactionId(transaction.getTransactionid())
                .transactionStatus(mapStatus(transaction.getTransactionStatus()))
                .build()
        );
    }

    public ResponseEntity<ProductInformationTransactionResponse> getProductInformationByTransactionId(Integer transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));

        Post post = transaction.getPost();
        ProductInformationTransactionResponse.ProductInformationTransactionResponseBuilder builder = ProductInformationTransactionResponse.builder();

        builder.title(post.getTitle());

        if (post.getVehicle() != null) {
            builder.type("VEHICLE")
                    .brand(post.getVehicle().getBrand())
                    .model(post.getVehicle().getModel())
                    .version(post.getVehicle().getVersion())
                    .odo(post.getVehicle().getOdo())
                    .batteryCapacity(post.getVehicle().getBatteryCapacity())
                    .yearManufacture(post.getVehicle().getYearManufacture())
                    .color(post.getVehicle().getColor())
                    .price(post.getPrice());
        } else if (post.getBattery() != null) {
            builder.type("BATTERY")
                    .brand(post.getBattery().getBatteryTypeId() != null ? post.getBattery().getBatteryTypeId().getTypename() : null)
                    .model(post.getBattery().getSerialNumber())
                    .batteryCapacity(post.getBattery().getOriginCapacity())
                    .odo(post.getBattery().getMileageCovered());
        }

        return ResponseEntity.ok(builder.build());
    }

    private String mapStatus(String status) {
        if (status == null) return null;
        return switch (status.toUpperCase()) {
            case "DONE" -> "PAID";
            case "FAIL" -> "FAILED";
            default -> status.toUpperCase();
        };
    }
}
