package com.upi.auth_service.controller;

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
}