package com.nik07roxx.apexPay.strategy;

import com.nik07roxx.apexPay.DTO.Transactions.StrategyConversionResult;
import com.nik07roxx.apexPay.DTO.Transactions.TransferRequest;
import com.nik07roxx.apexPay.DTO.Transactions.TransactionResponse;
import com.nik07roxx.apexPay.Entity.Account;

public interface PaymentStrategy {
    StrategyConversionResult processRouting(TransferRequest request, Account sourceAccount, Account targetAccount);
    String getRoutingType(); // Will return "SAME_CURRENCY" or "CROSS_CURRENCY"
}