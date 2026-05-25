package com.upi.auth_service.service;

import com.upi.auth_service.dto.*;
import com.upi.auth_service.entity.*;
import com.upi.auth_service.exception.BusinessException;
import com.upi.auth_service.exception.DuplicateTransactionException;
import com.upi.auth_service.exception.InsufficientBalanceException;
import com.upi.auth_service.repository.*;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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

    // ---- Wallet management ----

    @Transactional(rollbackFor = Exception.class)
    public WalletResponse createWallet(Authentication authentication) {
        User user = resolveUser(authentication);

        if (walletRepository.findByUser(user).isPresent()) {
            throw new BusinessException("A wallet already exists for this account.", HttpStatus.CONFLICT);
        }

        Wallet wallet = Wallet.builder()
                .user(user)
                .balance(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .build();

        wallet = walletRepository.save(wallet);

        return WalletResponse.builder()
                .walletId(wallet.getId())
                .ownerEmail(user.getEmail())
                .balance(wallet.getBalance())
                .createdAt(wallet.getCreatedAt())
                .build();
    }

    public BalanceResponse getBalance(Authentication authentication) {
        User user = resolveUser(authentication);
        Wallet wallet = resolveWallet(user);

        return BalanceResponse.builder()
                .email(user.getEmail())
                .balance(wallet.getBalance())
                .currency("INR")
                .build();
    }

    // ---- Money operations ----

    @Transactional(rollbackFor = Exception.class)
    public void addMoney(Authentication authentication, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Amount to add must be greater than zero.", HttpStatus.BAD_REQUEST);
        }

        User user = resolveUser(authentication);
        Wallet wallet = resolveWallet(user);

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
    }

    @Transactional(rollbackFor = Exception.class)
    public TransferResponse transferMoney(
            Authentication authentication,
            TransferRequest request,
            String idempotencyKey
    ) {
        // Idempotency check — return the cached result if key was already processed.
        if (StringUtils.hasText(idempotencyKey)) {
            transactionRepository.findByIdempotencyKey(idempotencyKey)
                    .ifPresent(existing -> {
                        throw new DuplicateTransactionException(
                                "Transaction with idempotency key '" + idempotencyKey + "' was already processed."
                        );
                    });
        }

        User sender = resolveUser(authentication);
        User receiver = userRepository.findByEmail(request.getReceiverEmail())
                .orElseThrow(() -> new BusinessException(
                        "Receiver account not found for email: " + request.getReceiverEmail(),
                        HttpStatus.NOT_FOUND
                ));

        if (sender.getId().equals(receiver.getId())) {
            throw new BusinessException("You cannot transfer money to your own account.", HttpStatus.BAD_REQUEST);
        }

        Wallet senderWallet = resolveWallet(sender);
        Wallet receiverWallet = resolveWallet(receiver);

        BigDecimal amount = request.getAmount();

        if (senderWallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance. Available: " + senderWallet.getBalance() + " INR."
            );
        }

        // Debit sender and credit receiver — @Version on Wallet guarantees atomicity.
        senderWallet.setBalance(senderWallet.getBalance().subtract(amount));
        receiverWallet.setBalance(receiverWallet.getBalance().add(amount));

        try {
            walletRepository.save(senderWallet);
            walletRepository.save(receiverWallet);
        } catch (OptimisticLockingFailureException ex) {
            throw new DuplicateTransactionException(
                    "Concurrent modification detected. Please retry the transfer."
            );
        }

        String transactionId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

        Transaction transaction = Transaction.builder()
                .transactionId(transactionId)
                .idempotencyKey(StringUtils.hasText(idempotencyKey) ? idempotencyKey : null)
                .senderWallet(senderWallet)
                .receiverWallet(receiverWallet)
                .amount(amount)
                .status(TransactionStatus.SUCCESS)
                .createdAt(now)
                .build();

        transactionRepository.save(transaction);

        // Double-entry ledger: one DEBIT line and one CREDIT line per transfer.
        ledgerRepository.save(LedgerEntry.builder()
                .transactionId(transactionId)
                .wallet(senderWallet)
                .type(LedgerType.DEBIT)
                .amount(amount)
                .createdAt(now)
                .build());

        ledgerRepository.save(LedgerEntry.builder()
                .transactionId(transactionId)
                .wallet(receiverWallet)
                .type(LedgerType.CREDIT)
                .amount(amount)
                .createdAt(now)
                .build());

        return TransferResponse.builder()
                .transactionId(transactionId)
                .senderEmail(sender.getEmail())
                .receiverEmail(receiver.getEmail())
                .amount(amount)
                .status(TransactionStatus.SUCCESS.name())
                .timestamp(now)
                .build();
    }

    // ---- Transaction history ----

    public PagedResponse<TransactionResponse> getTransactions(
            Authentication authentication,
            int page,
            int size
    ) {
        User user = resolveUser(authentication);
        Wallet wallet = resolveWallet(user);

        Page<Transaction> txPage = transactionRepository.findByWallet(
                wallet,
                PageRequest.of(page, size)
        );

        // Map each transaction to a clean DTO, flagging direction for the caller.
        var content = txPage.getContent().stream()
                .map(tx -> TransactionResponse.builder()
                        .transactionId(tx.getTransactionId())
                        .senderEmail(tx.getSenderWallet().getUser().getEmail())
                        .receiverEmail(tx.getReceiverWallet().getUser().getEmail())
                        .amount(tx.getAmount())
                        .status(tx.getStatus().name())
                        .timestamp(tx.getCreatedAt())
                        .debit(tx.getSenderWallet().getId().equals(wallet.getId()))
                        .build())
                .toList();

        return PagedResponse.<TransactionResponse>builder()
                .content(content)
                .page(txPage.getNumber())
                .size(txPage.getSize())
                .totalElements(txPage.getTotalElements())
                .totalPages(txPage.getTotalPages())
                .last(txPage.isLast())
                .build();
    }

    // ---- Private helpers ----

    private User resolveUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new BusinessException("Authenticated user not found.", HttpStatus.UNAUTHORIZED));
    }

    private Wallet resolveWallet(User user) {
        return walletRepository.findByUser(user)
                .orElseThrow(() -> new BusinessException(
                        "No wallet found for this account. Please create a wallet first.",
                        HttpStatus.NOT_FOUND
                ));
    }
}