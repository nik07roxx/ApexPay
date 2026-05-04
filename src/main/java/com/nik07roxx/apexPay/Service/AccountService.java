package com.nik07roxx.apexPay.Service;

import com.nik07roxx.apexPay.DTO.Account.AccountCreationRequest;
import com.nik07roxx.apexPay.DTO.Account.AccountResponse;
import com.nik07roxx.apexPay.DTO.Account.AccountUpdationRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AccountService {
    AccountResponse createAccount(AccountCreationRequest accountRequest);
    Page<AccountResponse> findAllAccounts(Pageable pageable);
    Page<AccountResponse> findAccountsByCustomerId(Long customerId, Pageable pageable);
    AccountResponse findAccountByAccountNumber(String accountNumber);
    void deleteAccountById(String accountNumber);
    AccountResponse updateAccount(String accountNumber, AccountUpdationRequest accountRequest);
}
