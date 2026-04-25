package com.nik07roxx.apexPay.Service;

import com.nik07roxx.apexPay.DTO.Transactions.DepositRequest;
import com.nik07roxx.apexPay.DTO.Transactions.TransactionResponse;
import com.nik07roxx.apexPay.DTO.Transactions.TransferRequest;
import com.nik07roxx.apexPay.DTO.Transactions.WithdrawRequest;

import java.util.List;

public interface TransactionsService {
    TransactionResponse depositToAccount(DepositRequest depositRequest);
    TransactionResponse withdrawFromAccount(WithdrawRequest withdrawRequest);
    TransactionResponse transfer(TransferRequest transferRequest);
    List<TransactionResponse> findTransactionsByTransactionReference(String transactionReference);
    List<TransactionResponse> findTransactionsByAccountNumber(String accountNumber);
}
