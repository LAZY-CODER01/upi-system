package com.upi.auth_service.controller;

import com.upi.auth_service.dto.*;
import com.upi.auth_service.service.WalletService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    /** Create a wallet for the authenticated user. Returns 201 Created on success. */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<WalletResponse>> createWallet(Authentication authentication) {
        WalletResponse wallet = walletService.createWallet(authentication);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Wallet created successfully.", wallet));
    }

    /** Retrieve the current balance for the authenticated user's wallet. */
    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<BalanceResponse>> balance(Authentication authentication) {
        BalanceResponse balance = walletService.getBalance(authentication);
        return ResponseEntity.ok(ApiResponse.success(balance));
    }

    /** Add money to the authenticated user's wallet. */
    @PostMapping("/add-money")
    public ResponseEntity<ApiResponse<Void>> addMoney(
            Authentication authentication,
            @RequestParam
            @NotNull(message = "Amount must not be null")
            @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
            BigDecimal amount
    ) {
        walletService.addMoney(authentication, amount);
        return ResponseEntity.ok(ApiResponse.success("Money added successfully.", null));
    }

    /**
     * Transfer money to another user.
     *
     * <p>Optionally supply {@code X-Idempotency-Key} header to prevent duplicate submissions.
     * The same key within the system's TTL window will return 409 Conflict.
     */
    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransferResponse>> transfer(
            Authentication authentication,
            @Valid @RequestBody TransferRequest request,
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey
    ) {
        TransferResponse result = walletService.transferMoney(authentication, request, idempotencyKey);
        return ResponseEntity.ok(ApiResponse.success("Transfer completed successfully.", result));
    }

    /** Retrieve paginated transaction history for the authenticated user. */
    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<PagedResponse<TransactionResponse>>> transactions(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PagedResponse<TransactionResponse> result = walletService.getTransactions(authentication, page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}