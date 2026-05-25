package com.upi.auth_service.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Read-only projection of a transaction — safe to serialize and expose through the API. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

    private String transactionId;

    private String senderEmail;

    private String receiverEmail;

    private BigDecimal amount;

    private String status;

    private LocalDateTime timestamp;

    /** Indicates whether the authenticated user was the sender in this transaction. */
    private boolean debit;
}
