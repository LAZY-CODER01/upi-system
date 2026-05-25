package com.upi.auth_service.controller;

import com.upi.auth_service.dto.TransferRequest;
import com.upi.auth_service.entity.Wallet;
import com.upi.auth_service.service.WalletService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping("/create")
    public Wallet createWallet(
            Authentication authentication
    ) {

        return walletService.createWallet(authentication);
    }

    @GetMapping("/balance")
    public BigDecimal balance(
            Authentication authentication
    ) {

        return walletService.getBalance(authentication);
    }

    @PostMapping("/add-money")
    public String addMoney(
            Authentication authentication,
            @RequestParam BigDecimal amount
    ) {

        return walletService.addMoney(
                authentication,
                amount
        );
    }

    @PostMapping("/transfer")
    public String transfer(
            Authentication authentication,
            @RequestBody TransferRequest request
    ) {

        return walletService.transferMoney(
                authentication,
                request
        );
    }
}