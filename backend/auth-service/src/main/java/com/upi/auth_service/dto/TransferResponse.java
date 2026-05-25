package com.upi.auth_service.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Clean DTO returned to clients after a successful transfer. Never exposes raw entities. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferResponse {

    private String transactionId;

    private String senderEmail;

    private String receiverEmail;

    private BigDecimal amount;

    private String status;

    private LocalDateTime timestamp;
}
