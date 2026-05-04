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
import com.nik07roxx.apexPay.exceptions.AccountNotFoundException;
import com.nik07roxx.apexPay.exceptions.InsufficientFundsException;
import com.nik07roxx.apexPay.exceptions.InvalidRequestException;
import com.nik07roxx.apexPay.model.TransactionType;
import com.nik07roxx.apexPay.util.ReferenceNumberGenerator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionsServiceImpl implements TransactionsService {

    private final ReferenceNumberGenerator referenceNumberGenerator;
    private final TransactionsRepository transactionsRepository;
    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public TransactionResponse depositToAccount(DepositRequest depositRequest) {
        log.info("Depositing {} into account number: {}.",depositRequest.amount(), depositRequest.targetAccount());
        String accountNumber = depositRequest.targetAccount();
        // check if amount sent is positive
        if(BigDecimal.ZERO.compareTo(depositRequest.amount()) >= 0)
        {
            log.error("Deposit amount cannot be negative or zero: {}", depositRequest.amount());
            throw new InvalidRequestException("Deposit amount cannot be negative or zero.");
        }

        // Add amount to target account's balance and save it again
        log.info("Finding account with account number {} for deposit.", accountNumber);
        Account foundAccount = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> {
                    log.error("No account found with account number: {}", accountNumber);
                    return new AccountNotFoundException("Account not found: " + accountNumber);
                });
        BigDecimal currentBalance = foundAccount.getBalance();
        foundAccount.setBalance(currentBalance.add(depositRequest.amount()));
        log.info("Saving account with account number {} for deposit.", accountNumber);
        accountRepository.save(foundAccount);

        // create a new transaction and assign the data
        Transactions newTransaction = new Transactions();
        newTransaction.setTransactionType(TransactionType.DEPOSIT);
        newTransaction.setAmount(depositRequest.amount());
        newTransaction.setDescription(depositRequest.description());
        newTransaction.setTargetAccount(accountNumber);
        newTransaction.setTransactionReference(referenceNumberGenerator.generateUniqueTransactionReference());

        // Save the transaction
        log.info("Saving deposit transaction with reference {} for account number {}."
                ,newTransaction.getTransactionReference()
                ,accountNumber);
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
        log.info("Withdrawing {} from account number: {}.",withdrawRequest.amount(), withdrawRequest.sourceAccount());
        String accountNumber = withdrawRequest.sourceAccount();
        // check if amount sent is positive
        if(BigDecimal.ZERO.compareTo(withdrawRequest.amount()) >= 0)
        {
            log.error("Withdraw amount cannot be negative or zero: {}", withdrawRequest.amount());
            throw new InvalidRequestException ("Withdraw amount cannot be negative or zero.");
        }

        // Remove amount from source account's balance and save it again
        log.info("Finding account with account number {} for withdraw.", accountNumber);
        Account foundAccount = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> {
                    log.error("No account found with account number: {}", accountNumber);
                    return new AccountNotFoundException("Account not found: " + accountNumber);
                });
        BigDecimal currentBalance = foundAccount.getBalance();

        // check if current balance of account is greater than amount
        // Otherwise withdraw will fail
        if(currentBalance.compareTo(withdrawRequest.amount()) < 0)
        {
            log.error("Account has less balance({}) than withdraw request of {}", currentBalance, withdrawRequest.amount());
            throw new InsufficientFundsException("Account has less balance than withdraw request.");
        }
        foundAccount.setBalance(currentBalance.subtract(withdrawRequest.amount()));
        log.info("Saving account with account number {} for withdraw.", accountNumber);
        accountRepository.save(foundAccount);

        // create a new transaction and assign the data
        Transactions newTransaction = new Transactions();
        newTransaction.setTransactionType(TransactionType.WITHDRAWAL);
        newTransaction.setAmount(withdrawRequest.amount());
        newTransaction.setDescription(withdrawRequest.description());
        newTransaction.setSourceAccount(accountNumber);
        newTransaction.setTransactionReference(referenceNumberGenerator.generateUniqueTransactionReference());

        // Save the transaction
        log.info("Saving withdraw transaction with reference {} for account number {}."
                ,newTransaction.getTransactionReference()
                ,accountNumber);
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
        log.info("Transferring {} from account number: {} to account number: {}."
                ,transferRequest.amount()
                ,transferRequest.sourceAccount()
                ,transferRequest.targetAccount());

        String sourceAccountNumber = transferRequest.sourceAccount();
        String targetAccountNumber = transferRequest.targetAccount();

        // check if source and target accounts are the same
        if(sourceAccountNumber.equals(targetAccountNumber))
        {
            log.error("Source and Target accounts cannot be the same.");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Source and Target accounts cannot be the same.");
        }

        // check if amount sent is positive
        if(BigDecimal.ZERO.compareTo(transferRequest.amount()) >= 0)
        {
            log.error("Transfer amount cannot be negative or zero: {}", transferRequest.amount());
            throw new InvalidRequestException ("Transfer amount cannot be negative or zero.");
        }

        // Remove amount from source account's balance and save it again
        log.info("Finding source account with account number {} for transfer.", sourceAccountNumber);
        Account foundWithdrawAccount = accountRepository.findByAccountNumber(sourceAccountNumber)
                .orElseThrow(() -> {
                    log.error("No account found with account number: {}", sourceAccountNumber);
                    return new AccountNotFoundException("Account not found: " + sourceAccountNumber);
                });
        BigDecimal currentSourceBalance = foundWithdrawAccount.getBalance();

        // check if current balance of account is greater than amount
        // Otherwise withdraw will fail
        if(currentSourceBalance.compareTo(transferRequest.amount()) < 0)
        {
            log.error("Source Account {} has less balance than transfer request.", sourceAccountNumber);
            throw new InsufficientFundsException("Source Account has less balance than transfer request.");
        }
        foundWithdrawAccount.setBalance(currentSourceBalance.subtract(transferRequest.amount()));
        log.info("Saving account with account number {} for transfer.", sourceAccountNumber);
        accountRepository.save(foundWithdrawAccount);

        // Add amount to target account's balance and save it again
        log.info("Finding target account with account number {} for transfer.", targetAccountNumber);
        Account foundTargetAccount = accountRepository.findByAccountNumber(targetAccountNumber)
                .orElseThrow(() -> {
                    log.error("No account found with account number: {}", targetAccountNumber);
                    return new AccountNotFoundException("Account not found: " + targetAccountNumber);
                });
        BigDecimal currentTargetBalance = foundTargetAccount.getBalance();
        foundTargetAccount.setBalance(currentTargetBalance.add(transferRequest.amount()));
        log.info("Saving account with account number {} for transfer.", targetAccountNumber);
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
        log.info("Saving transfer transaction with reference {} for source account {} and tranfer account {}."
                ,newTransaction.getTransactionReference()
                ,sourceAccountNumber
                ,targetAccountNumber);
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
    public Page<TransactionResponse> findTransactionsByTransactionReference(String transactionReference,
                                                                            Pageable pageable) {
        log.info("Finding transactions with reference: {}", transactionReference);
        String cleanRef = transactionReference.replace("APX-","");
        // find all transactions starting with our transaction reference
        Page<Transactions> foundTransactions = transactionsRepository.
                findByTransactionReferenceStartingWith(cleanRef, pageable);

        return foundTransactions.map(transaction -> new TransactionResponse(
                "APX-"+transaction.getTransactionReference().substring(0, 8).toUpperCase(),
                transaction.getTransactionType(),
                transaction.getAmount(),
                transaction.getSourceAccount(),
                transaction.getTargetAccount(),
                transaction.getDescription(),
                transaction.getTimestamp()
        ));
    }

    @Override
    public Page<TransactionResponse> findTransactionsByAccountNumber(String accountNumber,
                                                                     Pageable pageable) {
        // find all transactions starting with our transaction reference
        log.info("Finding transactions for account: {}", accountNumber);
        Page<Transactions> foundTransactions = transactionsRepository.
                findBySourceAccountOrTargetAccountOrderByTimestampDesc(accountNumber,accountNumber,pageable);

        return foundTransactions.map(transaction -> new TransactionResponse(
                "APX-"+transaction.getTransactionReference().substring(0, 8).toUpperCase(),
                transaction.getTransactionType(),
                transaction.getAmount(),
                transaction.getSourceAccount(),
                transaction.getTargetAccount(),
                transaction.getDescription(),
                transaction.getTimestamp()
        ));
    }
}
