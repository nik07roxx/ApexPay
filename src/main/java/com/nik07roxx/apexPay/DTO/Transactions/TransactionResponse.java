package com.nik07roxx.apexPay.DTO.Transactions;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nik07roxx.apexPay.model.CurrencyType;
import com.nik07roxx.apexPay.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TransactionResponse (String transactionReference,
                                   TransactionType transactionType,
                                   String sourceAccount,
                                   BigDecimal sourceAmount,
                                   CurrencyType sourceCurrency,
                                   String sourceEmail,
                                   String targetAccount,
                                   BigDecimal targetAmount,
                                   CurrencyType targetCurrency,
                                   String targetEmail,
                                   BigDecimal exchangeRate,
                                   String description,
                                   LocalDateTime timestamp){}
