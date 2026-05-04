package com.nik07roxx.apexPay.Service;

import com.nik07roxx.apexPay.DTO.Transactions.DepositRequest;
import com.nik07roxx.apexPay.DTO.Transactions.TransactionResponse;
import com.nik07roxx.apexPay.DTO.Transactions.TransferRequest;
import com.nik07roxx.apexPay.DTO.Transactions.WithdrawRequest;
import jakarta.transaction.InvalidTransactionException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TransactionsService {
    TransactionResponse depositToAccount(DepositRequest depositRequest);
    TransactionResponse withdrawFromAccount(WithdrawRequest withdrawRequest);
    TransactionResponse transfer(TransferRequest transferRequest);
    Page<TransactionResponse> findTransactionsByTransactionReference(String transactionReference, Pageable pageable);
    Page<TransactionResponse> findTransactionsByAccountNumber(String accountNumber, Pageable pageable);
}
