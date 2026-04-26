package com.nik07roxx.apexPay.DTO.Transactions;

import com.nik07roxx.apexPay.model.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record DepositRequest(
                             @Positive(message = "Amount must be greater than zero")
                             BigDecimal amount,
                             @NotNull(message = "Account Number is required")
                             String targetAccount,
                             @Size(max = 255, message = "Description too long")
                             String description){}
