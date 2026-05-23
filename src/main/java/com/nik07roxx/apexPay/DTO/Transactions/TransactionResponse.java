package com.nik07roxx.apexPay.DTO.Transactions;

import com.nik07roxx.apexPay.model.CurrencyType;
import com.nik07roxx.apexPay.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse (String transactionReference,
                                   TransactionType transactionType,
                                   String sourceAccount,
                                   BigDecimal sourceAmount,
                                   CurrencyType sourceCurrency,
                                   String targetAccount,
                                   BigDecimal targetAmount,
                                   CurrencyType targetCurrency,
                                   BigDecimal exchangeRate,
                                   String description,
                                   LocalDateTime timestamp){}
