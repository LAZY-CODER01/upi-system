package com.upi.auth_service.service;

import com.upi.auth_service.dto.TransferRequest;
import com.upi.auth_service.entity.*;
import com.upi.auth_service.repository.LedgerRepository;
import com.upi.auth_service.repository.UserRepository;
import com.upi.auth_service.repository.WalletRepository;

import lombok.RequiredArgsConstructor;

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

    public Wallet createWallet(
            Authentication authentication
    ) {

        User user = userRepository.findByEmail(
                authentication.getName()
        ).orElseThrow();

        if (walletRepository.findByUser(user).isPresent()) {
            throw new RuntimeException(
                    "Wallet already exists"
            );
        }

        Wallet wallet = Wallet.builder()
                .user(user)
                .balance(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .build();

        return walletRepository.save(wallet);
    }

    public BigDecimal getBalance(
            Authentication authentication
    ) {

        User user = userRepository.findByEmail(
                authentication.getName()
        ).orElseThrow();

        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow();

        return wallet.getBalance();
    }

    @Transactional
    public String addMoney(
            Authentication authentication,
            BigDecimal amount
    ) {

        User user = userRepository.findByEmail(
                authentication.getName()
        ).orElseThrow();

        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow();

        wallet.setBalance(
                wallet.getBalance().add(amount)
        );

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
    public String transferMoney(
            Authentication authentication,
            TransferRequest request
    ) {

        User sender = userRepository.findByEmail(
                authentication.getName()
        ).orElseThrow();

        User receiver = userRepository.findByEmail(
                request.getReceiverEmail()
        ).orElseThrow(() ->
                new RuntimeException("Receiver not found"));

        Wallet senderWallet = walletRepository.findByUser(sender)
                .orElseThrow();

        Wallet receiverWallet = walletRepository.findByUser(receiver)
                .orElseThrow();

        BigDecimal amount = request.getAmount();

        if (senderWallet.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException(
                    "Insufficient balance"
            );
        }

        senderWallet.setBalance(
                senderWallet.getBalance().subtract(amount)
        );

        receiverWallet.setBalance(
                receiverWallet.getBalance().add(amount)
        );

        walletRepository.save(senderWallet);
        walletRepository.save(receiverWallet);

        String transactionId =
                UUID.randomUUID().toString();

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
}