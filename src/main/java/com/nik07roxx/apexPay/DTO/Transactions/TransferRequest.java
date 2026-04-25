package com.nik07roxx.apexPay.DTO.Transactions;

import com.nik07roxx.apexPay.model.TransactionType;

import java.math.BigDecimal;

public record TransferRequest (BigDecimal amount,
                               String sourceAccount,
                               String targetAccount,
                               String description){}
