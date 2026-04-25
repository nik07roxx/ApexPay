package com.nik07roxx.apexPay.Controller;
import java.util.List;

import com.nik07roxx.apexPay.DTO.Account.AccountCreationRequest;
import com.nik07roxx.apexPay.DTO.Account.AccountResponse;
import com.nik07roxx.apexPay.DTO.Account.AccountUpdationRequest;
import com.nik07roxx.apexPay.Service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@RequestBody AccountCreationRequest accountCreationRequest)
    {
        return new ResponseEntity<>(accountService.createAccount(accountCreationRequest),
                HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> findAllActiveAccounts()
    {
        return ResponseEntity.ok(accountService.findAllAccounts());
    }

    @GetMapping("/customer/{id}")
    public ResponseEntity<List<AccountResponse>> findAccountsForCustomer(@PathVariable Long id)
    {
        return ResponseEntity.ok(accountService.findAccountsByCustomerId(id));
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountResponse> findAccountByAccountNumber(@PathVariable String accountNumber)
    {
        return ResponseEntity.ok(accountService.findAccountByAccountNumber(accountNumber));
    }

    @DeleteMapping("/{accountNumber}")
    public ResponseEntity<Void> deleteAccountByAccountNumber(@PathVariable String accountNumber)
    {
        accountService.deleteAccountById(accountNumber);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{accountNumber}")
    public ResponseEntity<AccountResponse> updateAccount(@PathVariable String accountNumber,
                                                         @RequestBody AccountUpdationRequest accountUpdationRequest)
    {
        return ResponseEntity.ok(accountService.updateAccount(accountNumber, accountUpdationRequest));
    }
}
