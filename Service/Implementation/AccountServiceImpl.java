package com.nik07roxx.apexPay.Service.Implementation;

import com.nik07roxx.apexPay.DTO.Account.AccountCreationRequest;
import com.nik07roxx.apexPay.DTO.Account.AccountResponse;
import com.nik07roxx.apexPay.DTO.Account.AccountUpdationRequest;
import com.nik07roxx.apexPay.DTO.Customer.CustomerResponse;
import com.nik07roxx.apexPay.Entity.Account;
import com.nik07roxx.apexPay.Entity.Customer;
import com.nik07roxx.apexPay.Repository.AccountRepository;
import com.nik07roxx.apexPay.Repository.CustomerRepository;
import com.nik07roxx.apexPay.Service.AccountService;
import com.nik07roxx.apexPay.model.AccountStatus;
import com.nik07roxx.apexPay.model.AccountType;
import com.nik07roxx.apexPay.util.ReferenceNumberGenerator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final ReferenceNumberGenerator referenceNumberGenerator;

    @Override
    @Transactional
    public AccountResponse createAccount(AccountCreationRequest accountRequest) {
        // validate if the customer exists
        Customer customer = customerRepository.findById(accountRequest.customerId())
                        .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "No customer found with id: "
                        + accountRequest.customerId()));

        // Create an account object and populate it
        Account account = new Account();
        account.setAccountType(accountRequest.accountType());
        account.setStatus(AccountStatus.ACTIVE);
        account.setCustomer(customer);
        account.setAccountNumber(referenceNumberGenerator.generateUniqueAccountNumber());

        if(accountRequest.balance() == null)
            account.setBalance(BigDecimal.ZERO);
        else
            account.setBalance(accountRequest.balance());

        if(accountRequest.accountType() == AccountType.SAVINGS)
            account.setInterestRate(BigDecimal.valueOf(6));
        else if(accountRequest.accountType() == AccountType.CURRENT)
            account.setInterestRate(BigDecimal.ZERO);

        // Save the account
        Account savedAccount = accountRepository.save(account);

        AccountResponse accountResponse = new AccountResponse(
                savedAccount.getAccountNumber(),
                savedAccount.getAccountType(),
                savedAccount.getBalance(),
                savedAccount.getInterestRate(),
                savedAccount.getOpeningDate(),
                savedAccount.getStatus()
        );
        return accountResponse;
    }

    @Override
    public List<AccountResponse> findAllAccounts() {
        List<Account> allAccounts = accountRepository.findByStatus(AccountStatus.ACTIVE);
        List<AccountResponse> responseAccounts = new ArrayList<>();
        for(Account account: allAccounts)
        {
            AccountResponse accountResponse = new AccountResponse(
                    account.getAccountNumber(),
                    account.getAccountType(),
                    account.getBalance(),
                    account.getInterestRate(),
                    account.getOpeningDate(),
                    account.getStatus()
            );
            responseAccounts.add(accountResponse);
        }
        return responseAccounts;
    }

    @Override
    public List<AccountResponse> findAccountsByCustomerId(Long customerId) {
        List<Account> allCustomerAccounts = accountRepository.findByCustomerId(customerId);
        List<AccountResponse> responseAccounts = new ArrayList<>();
        for(Account account: allCustomerAccounts)
        {
            AccountResponse accountResponse = new AccountResponse(
                    account.getAccountNumber(),
                    account.getAccountType(),
                    account.getBalance(),
                    account.getInterestRate(),
                    account.getOpeningDate(),
                    account.getStatus()
            );
            responseAccounts.add(accountResponse);
        }
        return responseAccounts;
    }

    @Override
    public AccountResponse findAccountByAccountNumber(String accountNumber) {
        Account foundAccount = accountRepository.findByAccountNumber(accountNumber)
                        .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Account not found: "+accountNumber));
        AccountResponse accountResponse = new AccountResponse(
                foundAccount.getAccountNumber(),
                foundAccount.getAccountType(),
                foundAccount.getBalance(),
                foundAccount.getInterestRate(),
                foundAccount.getOpeningDate(),
                foundAccount.getStatus()
        );
        return accountResponse;
    }

    @Override
    @Transactional
    public void deleteAccountById(String accountNumber) {
        Account foundAccount = accountRepository.findByAccountNumber(accountNumber)
                        .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Account not found: "+accountNumber));

        // Check if balance is 0 before closing (good banking practice!)
        if (foundAccount.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot close account with remaining balance");
        }

        foundAccount.setStatus(AccountStatus.CLOSED);
        accountRepository.save(foundAccount);
    }

    @Override
    @Transactional
    public AccountResponse updateAccount(String accountNumber, AccountUpdationRequest accountRequest) {
        // validate if customer exists
        Account foundAccount = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Account not found: "+accountNumber));

        if(accountRequest.accountType() != null)
            foundAccount.setAccountType(accountRequest.accountType());
        if(accountRequest.balance() != null)
            foundAccount.setBalance(accountRequest.balance());
        if(accountRequest.accountType() != null)
        {
            if(accountRequest.accountType() == AccountType.SAVINGS)
                foundAccount.setInterestRate(BigDecimal.valueOf(6));
            if(accountRequest.accountType() == AccountType.CURRENT)
                foundAccount.setInterestRate(BigDecimal.ZERO);
        }

        Account savedAccount = accountRepository.save(foundAccount);

        AccountResponse accountResponse = new AccountResponse(
                savedAccount.getAccountNumber(),
                savedAccount.getAccountType(),
                savedAccount.getBalance(),
                savedAccount.getInterestRate(),
                savedAccount.getOpeningDate(),
                savedAccount.getStatus()
        );
        return accountResponse;
    }
}
