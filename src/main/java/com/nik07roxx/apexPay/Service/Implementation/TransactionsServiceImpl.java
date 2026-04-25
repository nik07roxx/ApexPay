package com.nik07roxx.apexPay.Service.Implementation;

import com.nik07roxx.apexPay.DTO.Transactions.DepositRequest;
import com.nik07roxx.apexPay.DTO.Transactions.TransactionResponse;
import com.nik07roxx.apexPay.DTO.Transactions.TransferRequest;
import com.nik07roxx.apexPay.DTO.Transactions.WithdrawRequest;
import com.nik07roxx.apexPay.Entity.Account;
import com.nik07roxx.apexPay.Entity.Transactions;
import com.nik07roxx.apexPay.Repository.AccountRepository;
import com.nik07roxx.apexPay.Repository.TransactionsRepository;
import com.nik07roxx.apexPay.Service.TransactionsService;
import com.nik07roxx.apexPay.model.TransactionType;
import com.nik07roxx.apexPay.util.ReferenceNumberGenerator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionsServiceImpl implements TransactionsService {

    private final ReferenceNumberGenerator referenceNumberGenerator;
    private final TransactionsRepository transactionsRepository;
    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public TransactionResponse depositToAccount(DepositRequest depositRequest) {
        String accountNumber = depositRequest.targetAccount();
        // check if amount sent is positive
        if(BigDecimal.ZERO.compareTo(depositRequest.amount()) >= 0)
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Deposit amount cannot be negative or zero.");
        }

        // Add amount to target account's balance and save it again
        Account foundAccount = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Account not found: "+accountNumber));
        BigDecimal currentBalance = foundAccount.getBalance();
        foundAccount.setBalance(currentBalance.add(depositRequest.amount()));
        accountRepository.save(foundAccount);

        // create a new transaction and assign the data
        Transactions newTransaction = new Transactions();
        newTransaction.setTransactionType(TransactionType.DEPOSIT);
        newTransaction.setAmount(depositRequest.amount());
        newTransaction.setDescription(depositRequest.description());
        newTransaction.setTargetAccount(accountNumber);
        newTransaction.setTransactionReference(referenceNumberGenerator.generateUniqueTransactionReference());

        // Save the transaction
        Transactions savedTransactions = transactionsRepository.save(newTransaction);

        // Create a transaction response and return it
        TransactionResponse transactionResponse = new TransactionResponse(
                "APX-"+savedTransactions.getTransactionReference().substring(0, 8).toUpperCase(),
                savedTransactions.getTransactionType(),
                savedTransactions.getAmount(),
                savedTransactions.getSourceAccount(),
                savedTransactions.getTargetAccount(),
                savedTransactions.getDescription(),
                savedTransactions.getTimestamp()
        );
        return transactionResponse;
    }

    @Override
    @Transactional
    public TransactionResponse withdrawFromAccount(WithdrawRequest withdrawRequest) {
        String accountNumber = withdrawRequest.sourceAccount();
        // check if amount sent is positive
        if(BigDecimal.ZERO.compareTo(withdrawRequest.amount()) >= 0)
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Withdraw amount cannot be negative or zero.");
        }

        // Remove amount from source account's balance and save it again
        Account foundAccount = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Account not found: "+accountNumber));
        BigDecimal currentBalance = foundAccount.getBalance();

        // check if current balance of account is greater than amount
        // Otherwise withdraw will fail
        if(currentBalance.compareTo(withdrawRequest.amount()) < 0)
        {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Account has less balance than withdraw request.");
        }
        foundAccount.setBalance(currentBalance.subtract(withdrawRequest.amount()));
        accountRepository.save(foundAccount);

        // create a new transaction and assign the data
        Transactions newTransaction = new Transactions();
        newTransaction.setTransactionType(TransactionType.WITHDRAWAL);
        newTransaction.setAmount(withdrawRequest.amount());
        newTransaction.setDescription(withdrawRequest.description());
        newTransaction.setSourceAccount(accountNumber);
        newTransaction.setTransactionReference(referenceNumberGenerator.generateUniqueTransactionReference());

        // Save the transaction
        Transactions savedTransactions = transactionsRepository.save(newTransaction);

        // Create a transaction response and return it
        TransactionResponse transactionResponse = new TransactionResponse(
                "APX-"+savedTransactions.getTransactionReference().substring(0, 8).toUpperCase(),
                savedTransactions.getTransactionType(),
                savedTransactions.getAmount(),
                savedTransactions.getSourceAccount(),
                savedTransactions.getTargetAccount(),
                savedTransactions.getDescription(),
                savedTransactions.getTimestamp()
        );
        return transactionResponse;
    }

    @Override
    @Transactional
    public TransactionResponse transfer(TransferRequest transferRequest) {
        String sourceAccountNumber = transferRequest.sourceAccount();
        String targetAccountNumber = transferRequest.targetAccount();

        // check if source and target accounts are the same
        if(sourceAccountNumber.equals(targetAccountNumber))
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Source and Target accounts cannot be the same.");
        }

        // check if amount sent is positive
        if(BigDecimal.ZERO.compareTo(transferRequest.amount()) >= 0)
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Transfer amount cannot be negative or zero.");
        }

        // Remove amount from source account's balance and save it again
        Account foundWithdrawAccount = accountRepository.findByAccountNumber(sourceAccountNumber)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Source Account not found: "+sourceAccountNumber));
        BigDecimal currentSourceBalance = foundWithdrawAccount.getBalance();

        // check if current balance of account is greater than amount
        // Otherwise withdraw will fail
        if(currentSourceBalance.compareTo(transferRequest.amount()) < 0)
        {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Source Account has less balance than transfer request.");
        }
        foundWithdrawAccount.setBalance(currentSourceBalance.subtract(transferRequest.amount()));
        accountRepository.save(foundWithdrawAccount);

        // Add amount to target account's balance and save it again
        Account foundTargetAccount = accountRepository.findByAccountNumber(targetAccountNumber)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Account not found: "+targetAccountNumber));
        BigDecimal currentTargetBalance = foundTargetAccount.getBalance();
        foundTargetAccount.setBalance(currentTargetBalance.add(transferRequest.amount()));
        accountRepository.save(foundTargetAccount);

        // create a new transaction and assign the data
        Transactions newTransaction = new Transactions();
        newTransaction.setTransactionType(TransactionType.TRANSFER);
        newTransaction.setAmount(transferRequest.amount());
        newTransaction.setDescription(transferRequest.description());
        newTransaction.setSourceAccount(sourceAccountNumber);
        newTransaction.setTargetAccount(targetAccountNumber);
        newTransaction.setTransactionReference(referenceNumberGenerator.generateUniqueTransactionReference());

        // Save the transaction
        Transactions savedTransactions = transactionsRepository.save(newTransaction);

        // Create a transaction response and return it
        TransactionResponse transactionResponse = new TransactionResponse(
                "APX-"+savedTransactions.getTransactionReference().substring(0, 8).toUpperCase(),
                savedTransactions.getTransactionType(),
                savedTransactions.getAmount(),
                savedTransactions.getSourceAccount(),
                savedTransactions.getTargetAccount(),
                savedTransactions.getDescription(),
                savedTransactions.getTimestamp()
        );
        return transactionResponse;
    }

    @Override
    public List<TransactionResponse> findTransactionsByTransactionReference(String transactionReference) {
        String cleanRef = transactionReference.replace("APX-","");
        List<TransactionResponse> transactionResponses = new ArrayList<>();

        // find all transactions starting with our transaction reference
        List<Transactions> foundTransactions = transactionsRepository.
                findByTransactionReferenceStartingWith(cleanRef);

        // Format them and add them to a list of the response DTO
        foundTransactions.forEach(transaction -> {
            TransactionResponse transactionResponse = new TransactionResponse(
                    "APX-"+transaction.getTransactionReference().substring(0, 8).toUpperCase(),
                    transaction.getTransactionType(),
                    transaction.getAmount(),
                    transaction.getSourceAccount(),
                    transaction.getTargetAccount(),
                    transaction.getDescription(),
                    transaction.getTimestamp()
            );
            transactionResponses.add(transactionResponse);
        });
        return transactionResponses;
    }

    @Override
    public List<TransactionResponse> findTransactionsByAccountNumber(String accountNumber) {
        List<TransactionResponse> transactionResponses = new ArrayList<>();

        // find all transactions starting with our transaction reference
        List<Transactions> foundTransactions = transactionsRepository.
                findBySourceAccountOrTargetAccountOrderByTimestampDesc(accountNumber,accountNumber);

        // Format them and add them to a list of the response DTO
        foundTransactions.forEach(transaction -> {
            TransactionResponse transactionResponse = new TransactionResponse(
                    "APX-"+transaction.getTransactionReference().substring(0, 8).toUpperCase(),
                    transaction.getTransactionType(),
                    transaction.getAmount(),
                    transaction.getSourceAccount(),
                    transaction.getTargetAccount(),
                    transaction.getDescription(),
                    transaction.getTimestamp()
            );
            transactionResponses.add(transactionResponse);
        });
        return transactionResponses;
    }
}
