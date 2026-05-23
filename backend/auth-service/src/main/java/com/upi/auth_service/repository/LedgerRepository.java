package com.upi.auth_service.repository;

import com.upi.auth_service.entity.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LedgerRepository
        extends JpaRepository<LedgerEntry, Long> {
}