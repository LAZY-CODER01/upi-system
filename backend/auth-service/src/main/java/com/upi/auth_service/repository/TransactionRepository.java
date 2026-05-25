package com.upi.auth_service.repository;

import com.upi.auth_service.entity.Transaction;
import com.upi.auth_service.entity.Wallet;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /** Lookup by client-supplied idempotency key for duplicate prevention. */
    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);

    /** Retrieve all transactions involving the given wallet, newest first. */
    @Query("""
            SELECT t FROM Transaction t
            WHERE t.senderWallet = :wallet OR t.receiverWallet = :wallet
            ORDER BY t.createdAt DESC
            """)
    Page<Transaction> findByWallet(@Param("wallet") Wallet wallet, Pageable pageable);
}