package com.g_wuy.swp391.voltera.service;

import com.g_wuy.swp391.voltera.entity.Account;
import com.g_wuy.swp391.voltera.entity.Contract;
import com.g_wuy.swp391.voltera.entity.Post;
import com.g_wuy.swp391.voltera.entity.Transaction;
import com.g_wuy.swp391.voltera.entity.User;
import com.g_wuy.swp391.voltera.mapper.ContractMapper;
import com.g_wuy.swp391.voltera.mapper.TransactionMapper;
import com.g_wuy.swp391.voltera.model.request.ContractRequest;
import com.g_wuy.swp391.voltera.model.response.ContractResponse;
import com.g_wuy.swp391.voltera.model.response.TransactionResponse;
import com.g_wuy.swp391.voltera.repository.AccountRepository;
import com.g_wuy.swp391.voltera.repository.ContractRepository;
import com.g_wuy.swp391.voltera.repository.PostRepository;
import com.g_wuy.swp391.voltera.repository.TransactionRepository;
import com.g_wuy.swp391.voltera.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ContractService {

    ContractMapper contractMapper;

    UserRepository userRepository;

    PostRepository postRepository;

    ContractRepository contractRepository;

    EmailService emailService;

    TransactionRepository transactionRepository;

    S3Service s3Service;

    TransactionMapper transactionMapper;

    NotificationService notificationService;

    AccountRepository accountRepository;

    @Transactional
    public ContractResponse createContract(ContractRequest request, String username) {

        // Get user account to check role
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        // Only buyers can create contracts
        if (!"BUYER".equalsIgnoreCase(account.getRole())) {
            if ("SELLER".equalsIgnoreCase(account.getRole())) {
                throw new RuntimeException("Sellers cannot create contracts for their own products. Only buyers can create contracts.");
            } else if ("ADMIN".equalsIgnoreCase(account.getRole())) {
                throw new RuntimeException("Administrators cannot create contracts. Only buyers can create contracts.");
            } else {
                throw new RuntimeException("Only registered buyers can create contracts. Please ensure you have the correct account type.");
            }
        }

        User buyer = userRepository.findUserByUsername(username);

        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new RuntimeException("Post not found"));
        User seller = post.getSellerId();


        Contract contract = contractMapper.toEntity(request, post, buyer, seller);
        contract.setExpirationdate(LocalDate.now().plusDays(7));
        contract = contractRepository.save(contract);
        return contractMapper.toResponse(contract);
    }

    @Transactional
    public ContractResponse signContract(String username, Integer contractId) {
        User user = userRepository.findUserByUsername(username);
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contract not found"));

        if (contract.getExpirationdate() != null && contract.getExpirationdate().isBefore(LocalDate.now())) {
            contract.setContractstatus("CANCELLED");
            contractRepository.save(contract);
            notificationService.sendForEvent(contract);
            throw new RuntimeException("This contract has expired and cannot be signed");
        }
        if (user.getId().equals(contract.getBuyerid().getId())) {
            contract.setBuyersigned(true);
        } else if (user.getId().equals(contract.getSellerid().getId())) {
            contract.setSellersigned(true);
        } else {
            throw new RuntimeException("You are not part of this contract");
        }

        if (Boolean.TRUE.equals(contract.getBuyersigned()) && Boolean.TRUE.equals(contract.getSellersigned())) {
            contract.setContractstatus("SIGNED");
            contract.setSigneddate(LocalDate.now());

            Transaction tx = Transaction.builder()
                    .post(contract.getPostid())
                    .contractid(contract)
                    .transactionStatus("PENDING")
                    .price(contract.getPostid().getPrice())
                    .createAt(Instant.now())
                    .buyerid(contract.getBuyerid())
                    .build();

            transactionRepository.save(tx);
        }

        contractRepository.save(contract);
        notificationService.sendForEvent(contract);
        return contractMapper.toResponse(contract);
    }


    @Transactional
    public ContractResponse cancelContract(String username, Integer contractId) {
        User user = userRepository.findUserByUsername(username);
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contract not found"));

        if (!contract.getBuyerid().getId().equals(user.getId()) &&
                !contract.getSellerid().getId().equals(user.getId())) {
            throw new RuntimeException("You cannot cancel this contract");
        }

        if ("SIGNED".equals(contract.getContractstatus())) {
            throw new RuntimeException("Cannot cancel a signed contract");
        }

        contract.setContractstatus("CANCELLED");
        contractRepository.save(contract);
        notificationService.sendForEvent(contract);
        return contractMapper.toResponse(contract);
    }

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void autoCancelExpiredContracts() {
        List<Contract> expiredContracts = contractRepository
                .findByContractstatusAndExpirationdateBefore("PENDING", LocalDate.now());

        for (Contract contract : expiredContracts) {
            boolean buyerSigned = Boolean.TRUE.equals(contract.getBuyersigned());
            boolean sellerSigned = Boolean.TRUE.equals(contract.getSellersigned());


            if (!(buyerSigned && sellerSigned)) {
                contract.setContractstatus("CANCELLED");
                contractRepository.save(contract);
                notificationService.sendForEvent(contract);
            }
        }

        if (!expiredContracts.isEmpty()) {
            System.out.println("Auto-cancelled " + expiredContracts.size() + " expired contracts.");
        }
    }

    @Transactional
    public ContractResponse uploadPdf(Integer id, MultipartFile file) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contract not found"));


        String pdfUrl = s3Service.uploadFile(file, "contracts");
        contract.setContractfile(pdfUrl);
        contractRepository.save(contract);
        emailService.sendContractEmailWithAttachment(contract, file);

        return contractMapper.toResponse(contract);
    }

    @Transactional
    public ContractResponse getContractById(Integer contractId, String username) {
        User user = userRepository.findUserByUsername(username);
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contract not found"));
        if (!contract.getBuyerid().getId().equals(user.getId()) &&
                !contract.getSellerid().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied to this contract");
        }


        return contractMapper.toResponse(contract);
    }

    public List<ContractResponse> getContractsForUser(String username) {
        return contractRepository.findActiveContractsByUser(username)
                .stream()
                .map(contractMapper::toResponse)
                .toList();
    }


    public List<TransactionResponse> getTransactionsForUser(String username) {
        return transactionRepository.findTransactionsByUser(username)
                .stream()
                .map(transactionMapper::toResponse)
                .toList();
    }

    @Transactional
    public String createPaymentForContract(Integer contractId, String username) {
        User user = userRepository.findUserByUsername(username);
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contract not found"));

        // Kiểm tra quyền truy cập
        if (!contract.getBuyerid().getId().equals(user.getId()) &&
                !contract.getSellerid().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied to this contract");
        }

        // Kiểm tra contract đã được ký bởi cả hai bên
        if (!"SIGNED".equals(contract.getContractstatus()) ||
                !Boolean.TRUE.equals(contract.getBuyersigned()) ||
                !Boolean.TRUE.equals(contract.getSellersigned())) {
            throw new RuntimeException("Contract must be signed by both parties before payment");
        }

        // Tìm transaction hiện có hoặc tạo mới nếu chưa có
        List<Transaction> existingTransactions = contract.getTransactions();
        Transaction transaction;

        if (existingTransactions != null && !existingTransactions.isEmpty()) {
            // Sử dụng transaction đầu tiên nếu đã tồn tại
            transaction = existingTransactions.get(0);
        } else {
            // Tạo transaction mới nếu chưa có
            transaction = Transaction.builder()
                    .post(contract.getPostid())
                    .contractid(contract)
                    .transactionStatus("PENDING")
                    .price(contract.getPostid().getPrice())
                    .createAt(Instant.now())
                    .buyerid(contract.getBuyerid())
                    .build();

            transaction = transactionRepository.save(transaction);
        }

        return transaction.getTransactionid().toString();
    }
}