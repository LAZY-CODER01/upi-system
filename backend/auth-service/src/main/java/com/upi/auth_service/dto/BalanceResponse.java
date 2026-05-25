package com.upi.auth_service.dto;

import lombok.*;

import java.math.BigDecimal;

/** Wraps wallet balance in a typed DTO instead of returning a raw BigDecimal. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceResponse {

    private String email;

    private BigDecimal balance;

    private String currency;
}
