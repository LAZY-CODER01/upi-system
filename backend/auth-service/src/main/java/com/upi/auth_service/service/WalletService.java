package com.upi.auth_service.service;

import com.upi.auth_service.entity.User;
import com.upi.auth_service.entity.Wallet;
import com.upi.auth_service.repository.UserRepository;
import com.upi.auth_service.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;

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
}