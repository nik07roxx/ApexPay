package com.nik07roxx.apexPay.Controller;

import com.nik07roxx.apexPay.DTO.Transactions.DepositRequest;
import com.nik07roxx.apexPay.DTO.Transactions.TransactionResponse;
import com.nik07roxx.apexPay.DTO.Transactions.TransferRequest;
import com.nik07roxx.apexPay.DTO.Transactions.WithdrawRequest;
import com.nik07roxx.apexPay.Service.Implementation.TransactionsServiceImpl;
import com.nik07roxx.apexPay.Service.TransactionsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/transactions")
public class TransactionsController {
    private final TransactionsService transactionsService;

    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponse> deposit(@Valid @RequestBody DepositRequest depositRequest)
    {
        return new ResponseEntity<>(transactionsService.depositToAccount(depositRequest), HttpStatus.CREATED);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(@Valid @RequestBody WithdrawRequest withdrawRequest)
    {
        return new ResponseEntity<>(transactionsService.withdrawFromAccount(withdrawRequest), HttpStatus.CREATED);
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(@Valid @RequestBody TransferRequest transferRequest)
    {
        return new ResponseEntity<>(transactionsService.transfer(transferRequest), HttpStatus.CREATED);
    }

    @GetMapping("/reference/{transactionReference}")
    public ResponseEntity<List<TransactionResponse>> findTransactionsByTransactionReference
            (@PathVariable String transactionReference)
    {
        return ResponseEntity.ok(transactionsService.findTransactionsByTransactionReference(transactionReference));
    }

    @GetMapping("/statement/{accountNumber}")
    public ResponseEntity<List<TransactionResponse>> findTransactionsByAccountNumber
            (@PathVariable String accountNumber)
    {
        return ResponseEntity.ok(transactionsService.findTransactionsByAccountNumber(accountNumber));
    }
}
