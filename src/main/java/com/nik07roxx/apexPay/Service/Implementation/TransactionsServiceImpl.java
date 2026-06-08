package com.nik07roxx.apexPay.Service.Implementation;

import com.nik07roxx.apexPay.DTO.Currency.CurrencyConvertorResponse;
import com.nik07roxx.apexPay.DTO.Transactions.*;
import com.nik07roxx.apexPay.Entity.Account;
import com.nik07roxx.apexPay.Entity.Transactions;
import com.nik07roxx.apexPay.Repository.AccountRepository;
import com.nik07roxx.apexPay.Repository.TransactionsRepository;
import com.nik07roxx.apexPay.Service.TransactionsService;
import com.nik07roxx.apexPay.exceptions.AccountNotFoundException;
import com.nik07roxx.apexPay.exceptions.InsufficientFundsException;
import com.nik07roxx.apexPay.exceptions.InvalidRequestException;
import com.nik07roxx.apexPay.factory.PaymentStrategyFactory;
import com.nik07roxx.apexPay.model.AccountStatus;
import com.nik07roxx.apexPay.model.CurrencyType;
import com.nik07roxx.apexPay.model.TransactionType;
import com.nik07roxx.apexPay.strategy.PaymentStrategy;
import com.nik07roxx.apexPay.util.ReferenceNumberGenerator;
import io.micrometer.core.annotation.Timed;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionsServiceImpl implements TransactionsService {

    private final ApplicationEventPublisher eventPublisher;
    private final ReferenceNumberGenerator referenceNumberGenerator;
    private final TransactionsRepository transactionsRepository;
    private final AccountRepository accountRepository;
    private final CurrencyConvertorService currencyConvertorService;
    private final PaymentStrategyFactory strategyFactory;

    @Override
    @Transactional
    @Timed(value = "apexpay.core.deposit", description = "Time taken to complete a deposit transaction")
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

        if (!AccountStatus.ACTIVE.equals(foundAccount.getStatus())) {
            log.error("Cannot perform transaction on a closed account: {}", accountNumber);
            throw new InvalidRequestException("Cannot perform transaction on a closed account: "+accountNumber);
        }

        BigDecimal currentBalance = foundAccount.getBalance();
        foundAccount.setBalance(currentBalance.add(depositRequest.amount()));
        log.info("Saving account with account number {} for deposit.", accountNumber);
        accountRepository.save(foundAccount);

        // create a new transaction and assign the data
        Transactions newTransaction = new Transactions();
        newTransaction.setTransactionType(TransactionType.DEPOSIT);
        newTransaction.setSourceAccount("CASH");
        newTransaction.setTargetAccount(accountNumber);
        newTransaction.setSourceAmount(depositRequest.amount());
        newTransaction.setTargetAmount(depositRequest.amount());
        newTransaction.setSourceCurrency(foundAccount.getAccountCurrency());
        newTransaction.setTargetCurrency(foundAccount.getAccountCurrency());
        newTransaction.setExchangeRate(new BigDecimal("1.0"));
        newTransaction.setDescription(depositRequest.description());
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
                savedTransactions.getSourceAccount(),
                savedTransactions.getSourceAmount(),
                savedTransactions.getSourceCurrency(),
                foundAccount.getCustomer().getEmail(),
                savedTransactions.getTargetAccount(),
                savedTransactions.getTargetAmount(),
                savedTransactions.getTargetCurrency(),
                foundAccount.getCustomer().getEmail(),
                savedTransactions.getExchangeRate(),
                savedTransactions.getDescription(),
                savedTransactions.getTimestamp()
        );

        // publish to internal spring event board, to trigger kafka producer
        eventPublisher.publishEvent(new TransactionCompleteEvent(transactionResponse));

        return transactionResponse;
    }

    @Override
    @Transactional
    @Timed(value = "apexpay.core.withdraw", description = "Time taken to complete a withdraw transaction")
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

        if (!AccountStatus.ACTIVE.equals(foundAccount.getStatus())) {
            log.error("Cannot perform transaction on a closed account: {}", accountNumber);
            throw new InvalidRequestException("Cannot perform transaction on a closed account: "+accountNumber);
        }

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
        newTransaction.setSourceAccount(accountNumber);
        newTransaction.setTargetAccount("CASH");
        newTransaction.setSourceAmount(withdrawRequest.amount());
        newTransaction.setTargetAmount(withdrawRequest.amount());
        newTransaction.setSourceCurrency(foundAccount.getAccountCurrency());
        newTransaction.setTargetCurrency(foundAccount.getAccountCurrency());
        newTransaction.setExchangeRate(new BigDecimal("1.0"));
        newTransaction.setDescription(withdrawRequest.description());
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
                savedTransactions.getSourceAccount(),
                savedTransactions.getSourceAmount(),
                savedTransactions.getSourceCurrency(),
                foundAccount.getCustomer().getEmail(),
                savedTransactions.getTargetAccount(),
                savedTransactions.getTargetAmount(),
                savedTransactions.getTargetCurrency(),
                foundAccount.getCustomer().getEmail(),
                savedTransactions.getExchangeRate(),
                savedTransactions.getDescription(),
                savedTransactions.getTimestamp()
        );

        // publish to internal spring event board, to trigger kafka producer
        eventPublisher.publishEvent(new TransactionCompleteEvent(transactionResponse));

        return transactionResponse;
    }

    @Override
    @Transactional
    @Timed(value = "apexpay.core.transfer", description = "Time taken to complete a transfer transaction")
    public TransactionResponse transfer(TransferRequest transferRequest) {
        log.info("Transferring {} {} from account number: {} to account number: {}."
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

        if (!AccountStatus.ACTIVE.equals(foundWithdrawAccount.getStatus())) {
            log.error("Cannot perform transaction on a closed account: {}", sourceAccountNumber);
            throw new InvalidRequestException("Cannot perform transaction on a closed account: "+sourceAccountNumber);
        }

        BigDecimal currentSourceBalance = foundWithdrawAccount.getBalance();

        // check if current balance of account is greater than amount
        // Otherwise withdraw will fail
        if(currentSourceBalance.compareTo(transferRequest.amount()) < 0)
        {
            log.error("Source Account {} has less balance than transfer request.", sourceAccountNumber);
            throw new InsufficientFundsException("Source Account has less balance than transfer request.");
        }

        // Add amount to target account's balance and save it again
        log.info("Finding target account with account number {} for transfer.", targetAccountNumber);
        Account foundTargetAccount = accountRepository.findByAccountNumber(targetAccountNumber)
                .orElseThrow(() -> {
                    log.error("No account found with account number: {}", targetAccountNumber);
                    return new AccountNotFoundException("Account not found: " + targetAccountNumber);
                });

        if (!AccountStatus.ACTIVE.equals(foundTargetAccount.getStatus())) {
            log.error("Cannot perform transaction on a closed account: {}", targetAccountNumber);
            throw new InvalidRequestException("Cannot perform transaction on a closed account: "+targetAccountNumber);
        }

        BigDecimal currentTargetBalance = foundTargetAccount.getBalance();

        // check if both account currencies are the same or not
        CurrencyType sourceAccCurrency = foundWithdrawAccount.getAccountCurrency();
        CurrencyType targetAccCurrency = foundTargetAccount.getAccountCurrency();

        String routingType = sourceAccCurrency.toString().equalsIgnoreCase(targetAccCurrency.toString())
                ? "SAME_CURRENCY"
                : "CROSS_CURRENCY";

        PaymentStrategy strategy = strategyFactory.getStrategy(routingType);
        StrategyConversionResult result = strategy.processRouting(transferRequest,
                foundWithdrawAccount,
                foundTargetAccount);

        foundWithdrawAccount.setBalance(currentSourceBalance.subtract(result.debitAmount()));
        foundTargetAccount.setBalance(currentTargetBalance.add(result.creditAmount()));

        log.info("Saving account with account number {} for transfer.", sourceAccountNumber);
        accountRepository.save(foundWithdrawAccount);

        log.info("Saving account with account number {} for transfer.", targetAccountNumber);
        accountRepository.save(foundTargetAccount);

        // create a new transaction and assign the data
        Transactions newTransaction = new Transactions();
        newTransaction.setTransactionType(TransactionType.TRANSFER);
        newTransaction.setSourceAccount(sourceAccountNumber);
        newTransaction.setTargetAccount(targetAccountNumber);
        newTransaction.setSourceAmount(result.debitAmount());
        newTransaction.setTargetAmount(result.creditAmount());
        newTransaction.setSourceCurrency(sourceAccCurrency);
        newTransaction.setTargetCurrency(targetAccCurrency);
        newTransaction.setExchangeRate(result.rateOfConversion());
        newTransaction.setDescription(transferRequest.description());
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
                savedTransactions.getSourceAccount(),
                savedTransactions.getSourceAmount(),
                savedTransactions.getSourceCurrency(),
                foundWithdrawAccount.getCustomer().getEmail(),
                savedTransactions.getTargetAccount(),
                savedTransactions.getTargetAmount(),
                savedTransactions.getTargetCurrency(),
                foundTargetAccount.getCustomer().getEmail(),
                savedTransactions.getExchangeRate(),
                savedTransactions.getDescription(),
                savedTransactions.getTimestamp()
        );

        // publish to internal spring event board, to trigger kafka producer
        eventPublisher.publishEvent(new TransactionCompleteEvent(transactionResponse));

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
                transaction.getSourceAccount(),
                transaction.getSourceAmount(),
                transaction.getSourceCurrency(),
                null,
                transaction.getTargetAccount(),
                transaction.getTargetAmount(),
                transaction.getTargetCurrency(),
                null,
                transaction.getExchangeRate(),
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
                transaction.getSourceAccount(),
                transaction.getSourceAmount(),
                transaction.getSourceCurrency(),
                null,
                transaction.getTargetAccount(),
                transaction.getTargetAmount(),
                transaction.getTargetCurrency(),
                null,
                transaction.getExchangeRate(),
                transaction.getDescription(),
                transaction.getTimestamp()
        ));
    }

    @Override
    public String rateCheck(CurrencyType fromCurrency, CurrencyType toCurrency) {
        CurrencyConvertorResponse convertedCurrencyAmount = currencyConvertorService.getConvertedCurrencyAmount(fromCurrency);
        return "%s %s is equal to %s %s."
                .formatted(convertedCurrencyAmount.getRate(fromCurrency.toString()),
                        fromCurrency,
                        convertedCurrencyAmount.getRate(toCurrency.toString()),
                        toCurrency);
    }
}
