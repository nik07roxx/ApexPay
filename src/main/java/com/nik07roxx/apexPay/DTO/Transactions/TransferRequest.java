package com.nik07roxx.apexPay.DTO.Transactions;

import com.nik07roxx.apexPay.model.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record TransferRequest (
                               @Positive(message = "Transfer amount must be greater than zero")
                               BigDecimal amount,
                               @NotNull(message = "Source account is required")
                               String sourceAccount,
                               @NotNull(message = "Target account is required")
                               String targetAccount,
                               @Size(max = 255, message = "Description too long")
                               String description){}
