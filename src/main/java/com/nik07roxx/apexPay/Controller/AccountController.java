package com.nik07roxx.apexPay.Controller;
import java.util.List;

import com.nik07roxx.apexPay.DTO.Account.AccountCreationRequest;
import com.nik07roxx.apexPay.DTO.Account.AccountResponse;
import com.nik07roxx.apexPay.DTO.Account.AccountUpdationRequest;
import com.nik07roxx.apexPay.Service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api/v1/accounts")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Account Management", description = "Endpoints for Account Opening, Closing, Updation and Views")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    @PostMapping
    @Operation(summary = "Creating a new account")
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody AccountCreationRequest accountCreationRequest)
    {
        return new ResponseEntity<>(accountService.createAccount(accountCreationRequest),
                HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "List all active accounts in the database")
    public ResponseEntity<Page<AccountResponse>> findAllActiveAccounts(
            @Parameter(hidden = true) @PageableDefault(size = 10, page = 0, sort = "id") Pageable pageable,
            @Parameter(description = "Page number", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Size of page", example = "10") @RequestParam(defaultValue = "10") int size
    )
    {
        return ResponseEntity.ok(accountService.findAllAccounts(pageable));
    }

    @GetMapping("/customer/{id}")
    @Operation(summary = "List all accounts for the Customer Id")
    public ResponseEntity<Page<AccountResponse>> findAccountsForCustomer(@PathVariable Long id,
                                                                         @Parameter(hidden = true) @PageableDefault(size = 10, page = 0, sort = "id") Pageable pageable,
                                                                         @Parameter(description = "Page number", example = "0") @RequestParam(defaultValue = "0") int page,
                                                                         @Parameter(description = "Size of page", example = "10") @RequestParam(defaultValue = "10") int size)
    {
        return ResponseEntity.ok(accountService.findAccountsByCustomerId(id, pageable));
    }

    @GetMapping("/{accountNumber}")
    @Operation(summary = "Get an account")
    public ResponseEntity<AccountResponse> findAccountByAccountNumber(@PathVariable String accountNumber)
    {
        return ResponseEntity.ok(accountService.findAccountByAccountNumber(accountNumber));
    }

    @DeleteMapping("/{accountNumber}")
    @Operation(summary = "Delete an account")
    public ResponseEntity<Void> deleteAccountByAccountNumber(@PathVariable String accountNumber)
    {
        accountService.deleteAccountById(accountNumber);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{accountNumber}")
    @Operation(summary = "Update an account's details")
    public ResponseEntity<AccountResponse> updateAccount(@PathVariable String accountNumber,
                                                         @Valid @RequestBody AccountUpdationRequest accountUpdationRequest)
    {
        return ResponseEntity.ok(accountService.updateAccount(accountNumber, accountUpdationRequest));
    }
}
