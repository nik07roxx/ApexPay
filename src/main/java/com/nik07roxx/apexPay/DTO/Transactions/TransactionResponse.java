package com.nik07roxx.apexPay.DTO.Transactions;

import com.nik07roxx.apexPay.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse (String transactionReference,
                                   TransactionType transactionType,
                                   BigDecimal amount,
                                   String sourceAccount,
                                   String targetAccount,
                                   String description,
                                   LocalDateTime timestamp){}
