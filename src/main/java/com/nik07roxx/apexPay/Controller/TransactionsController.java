package com.nik07roxx.apexPay.Controller;

import com.nik07roxx.apexPay.DTO.Transactions.DepositRequest;
import com.nik07roxx.apexPay.DTO.Transactions.TransactionResponse;
import com.nik07roxx.apexPay.DTO.Transactions.TransferRequest;
import com.nik07roxx.apexPay.DTO.Transactions.WithdrawRequest;
import com.nik07roxx.apexPay.Service.Implementation.TransactionsServiceImpl;
import com.nik07roxx.apexPay.Service.TransactionsService;
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

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Transaction Management", description = "Endpoints for deposit, withdrawal, and transfers")
@RequestMapping("/api/v1/transactions")
public class TransactionsController {
    private final TransactionsService transactionsService;

    @PostMapping("/deposit")
    @Operation(summary = "Deposit money into an account")
    public ResponseEntity<TransactionResponse> deposit(@Valid @RequestBody DepositRequest depositRequest)
    {
        return new ResponseEntity<>(transactionsService.depositToAccount(depositRequest), HttpStatus.CREATED);
    }

    @PostMapping("/withdraw")
    @Operation(summary = "Withdraw money from an account")
    public ResponseEntity<TransactionResponse> withdraw(@Valid @RequestBody WithdrawRequest withdrawRequest)
    {
        return new ResponseEntity<>(transactionsService.withdrawFromAccount(withdrawRequest), HttpStatus.CREATED);
    }

    @PostMapping("/transfer")
    @Operation(summary = "Transfer money between two accounts")
    public ResponseEntity<TransactionResponse> transfer(@Valid @RequestBody TransferRequest transferRequest)
    {
        return new ResponseEntity<>(transactionsService.transfer(transferRequest), HttpStatus.CREATED);
    }

    @GetMapping("/reference/{transactionReference}")
    @Operation(summary = "Find a transaction using transaction reference")
    public ResponseEntity<Page<TransactionResponse>> findTransactionsByTransactionReference
            (@PathVariable String transactionReference,
             @Parameter(hidden = true) @PageableDefault(size = 10, page = 0, sort = "id") Pageable pageable,
             @Parameter(description = "Page number", example = "0") @RequestParam(defaultValue = "0") int page,
             @Parameter(description = "Size of page", example = "10") @RequestParam(defaultValue = "10") int size)
    {
        return ResponseEntity.ok(transactionsService.findTransactionsByTransactionReference(transactionReference, pageable));
    }

    @GetMapping("/statement/{accountNumber}")
    @Operation(summary = "List all transactions for an account")
    public ResponseEntity<Page<TransactionResponse>> findTransactionsByAccountNumber
            (@PathVariable String accountNumber,
             @Parameter(hidden = true) @PageableDefault(size = 10, page = 0, sort = "id") Pageable pageable,
             @Parameter(description = "Page number", example = "0") @RequestParam(defaultValue = "0") int page,
             @Parameter(description = "Size of page", example = "10") @RequestParam(defaultValue = "10") int size)
    {
        return ResponseEntity.ok(transactionsService.findTransactionsByAccountNumber(accountNumber, pageable));
    }
}
