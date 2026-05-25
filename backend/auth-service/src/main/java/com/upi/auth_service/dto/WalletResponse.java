package com.upi.auth_service.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Returned after wallet creation — exposes only safe fields, never the full entity. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletResponse {

    private Long walletId;

    private String ownerEmail;

    private BigDecimal balance;

    private LocalDateTime createdAt;
}
