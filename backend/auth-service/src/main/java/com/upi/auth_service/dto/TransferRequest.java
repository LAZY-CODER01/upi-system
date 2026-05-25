package com.upi.auth_service.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferRequest {

    @NotBlank(message = "Receiver email must not be blank")
    @Email(message = "Receiver email must be a valid email address")
    private String receiverEmail;

    @NotNull(message = "Amount must not be null")
    @DecimalMin(value = "0.01", message = "Transfer amount must be at least 0.01")
    @Digits(integer = 10, fraction = 2, message = "Amount format is invalid (max 10 integer digits, 2 decimal places)")
    private BigDecimal amount;
}