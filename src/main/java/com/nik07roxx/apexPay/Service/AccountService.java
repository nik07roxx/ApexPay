package com.nik07roxx.apexPay.Service;

import com.nik07roxx.apexPay.DTO.Account.AccountCreationRequest;
import com.nik07roxx.apexPay.DTO.Account.AccountResponse;
import com.nik07roxx.apexPay.DTO.Account.AccountUpdationRequest;

import java.util.List;

public interface AccountService {
    AccountResponse createAccount(AccountCreationRequest accountRequest);
    List<AccountResponse> findAllAccounts();
    List<AccountResponse> findAccountsByCustomerId(Long customerId);
    AccountResponse findAccountByAccountNumber(String accountNumber);
    void deleteAccountById(String accountNumber);
    AccountResponse updateAccount(String accountNumber, AccountUpdationRequest accountRequest);
}
