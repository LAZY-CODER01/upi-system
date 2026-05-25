package com.upi.auth_service.repository;

import com.upi.auth_service.entity.Transaction;
import com.upi.auth_service.entity.Wallet;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository
        extends JpaRepository<Transaction, Long> {

    Page<Transaction> findBySenderWalletOrReceiverWallet(
            Wallet senderWallet,
            Wallet receiverWallet,
            Pageable pageable
    );
}