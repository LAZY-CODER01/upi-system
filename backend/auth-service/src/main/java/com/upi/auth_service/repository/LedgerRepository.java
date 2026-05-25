package com.upi.auth_service.repository;

import com.upi.auth_service.entity.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LedgerRepository extends JpaRepository<LedgerEntry, Long> {

    /** Retrieve all ledger lines for a given transaction — useful for audit trails. */
    List<LedgerEntry> findByTransactionId(String transactionId);
}