package com.upi.auth_service.repository;

import com.upi.auth_service.entity.User;
import com.upi.auth_service.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletRepository
        extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByUser(User user);
}