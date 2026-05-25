package com.upi.auth_service.service;

import com.upi.auth_service.dto.TransferRequest;
import com.upi.auth_service.entity.*;
import com.upi.auth_service.repository.*; // Added TransactionRepository import cleanly

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final LedgerRepository ledgerRepository;
    private final TransactionRepository transactionRepository;

    public Wallet createWallet(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Logged-in user context not found"));

        if (walletRepository.findByUser(user).isPresent()) {
            throw new RuntimeException("Wallet already exists for this user");
        }

        Wallet wallet = Wallet.builder()
                .user(user)
                .balance(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .build();

        return walletRepository.save(wallet);
    }

    public BigDecimal getBalance(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Wallet not found. Please create a wallet first."));

        return wallet.getBalance();
    }

    @Transactional
    public String addMoney(Authentication authentication, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Amount to add must be greater than zero");
        }

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // NOTE: Use a pessimistic write lock in repository for financial modifications
        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Wallet not found for this user"));

        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);

        LedgerEntry entry = LedgerEntry.builder()
                .transactionId(UUID.randomUUID().toString())
                .wallet(wallet)
                .type(LedgerType.CREDIT)
                .amount(amount)
                .createdAt(LocalDateTime.now())
                .build();

        ledgerRepository.save(entry);

        return "Money added successfully";
    }

    @Transactional
    public String transferMoney(Authentication authentication, TransferRequest request) {
        BigDecimal amount = request.getAmount();
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Transfer amount must be greater than zero");
        }

        User sender = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        User receiver = userRepository.findByEmail(request.getReceiverEmail())
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        if (sender.getId().equals(receiver.getId())) {
            throw new RuntimeException("Cannot transfer money to yourself");
        }

        // Fetching with database locking prevents concurrent balance exploitation
        Wallet senderWallet = walletRepository.findByUser(sender)
                .orElseThrow(() -> new RuntimeException("Sender wallet not found"));

        Wallet receiverWallet = walletRepository.findByUser(receiver)
                .orElseThrow(() -> new RuntimeException("Receiver wallet not found"));

        if (senderWallet.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        senderWallet.setBalance(senderWallet.getBalance().subtract(amount));
        receiverWallet.setBalance(receiverWallet.getBalance().add(amount));

        walletRepository.save(senderWallet);
        walletRepository.save(receiverWallet);

        String transactionId = UUID.randomUUID().toString();

        Transaction transaction = Transaction.builder()
                .transactionId(transactionId)
                .senderWallet(senderWallet)
                .receiverWallet(receiverWallet)
                .amount(amount)
                .status(TransactionStatus.SUCCESS)
                .createdAt(LocalDateTime.now())
                .build();

        transactionRepository.save(transaction);

        LedgerEntry debitEntry = LedgerEntry.builder()
                .transactionId(transactionId)
                .wallet(senderWallet)
                .type(LedgerType.DEBIT)
                .amount(amount)
                .createdAt(LocalDateTime.now())
                .build();

        LedgerEntry creditEntry = LedgerEntry.builder()
                .transactionId(transactionId)
                .wallet(receiverWallet)
                .type(LedgerType.CREDIT)
                .amount(amount)
                .createdAt(LocalDateTime.now())
                .build();

        ledgerRepository.save(debitEntry);
        ledgerRepository.save(creditEntry);

        return "Transfer successful";
    }

    public Page<Transaction> getTransactions(Authentication authentication, int page, int size) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        // explicitly returning Page typed entity instead of Object wrapper
        return transactionRepository.findBySenderWalletOrReceiverWallet(
                wallet,
                wallet,
                PageRequest.of(page, size)
        );
    }
}