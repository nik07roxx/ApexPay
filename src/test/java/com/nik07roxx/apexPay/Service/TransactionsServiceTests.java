package com.nik07roxx.apexPay.Service;

import com.nik07roxx.apexPay.DTO.Transactions.DepositRequest;
import com.nik07roxx.apexPay.DTO.Transactions.TransactionResponse;
import com.nik07roxx.apexPay.DTO.Transactions.TransferRequest;
import com.nik07roxx.apexPay.DTO.Transactions.WithdrawRequest;
import com.nik07roxx.apexPay.Entity.Account;
import com.nik07roxx.apexPay.Entity.Transactions;
import com.nik07roxx.apexPay.Repository.AccountRepository;
import com.nik07roxx.apexPay.Repository.TransactionsRepository;
import com.nik07roxx.apexPay.Service.Implementation.TransactionsServiceImpl;
import com.nik07roxx.apexPay.exceptions.AccountNotFoundException;
import com.nik07roxx.apexPay.exceptions.InvalidRequestException;
import com.nik07roxx.apexPay.util.ReferenceNumberGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionsServiceTests {

    @InjectMocks
    private TransactionsServiceImpl transactionsService;

    @Mock
    private ReferenceNumberGenerator referenceNumberGenerator;

    @Mock
    private TransactionsRepository transactionsRepository;

    @Mock
    private AccountRepository accountRepository;

    @Test
    void testTransferSuccess()
    {
        // Setup
        Account sourceAcc = new Account();
        sourceAcc.setAccountNumber("101");
        sourceAcc.setBalance(new BigDecimal("500.00"));
        Account targetAcc = new Account();
        targetAcc.setAccountNumber("102");
        targetAcc.setBalance(new BigDecimal("100.00"));
        TransferRequest transferRequest = new TransferRequest(new BigDecimal("125.00"),
                sourceAcc.getAccountNumber(),
                targetAcc.getAccountNumber(),
                "Test Transfer"
                );

        // Act
        when(accountRepository.findByAccountNumber("101")).thenReturn(Optional.of(sourceAcc));
        when(accountRepository.findByAccountNumber("102")).thenReturn(Optional.of(targetAcc));
        when(referenceNumberGenerator.generateUniqueTransactionReference()).thenReturn("REF12345");
        when(transactionsRepository.save(any(Transactions.class))).thenAnswer(i -> i.getArguments()[0]);

        TransactionResponse response = transactionsService.transfer(transferRequest);

        assertEquals(new BigDecimal("375.00"), sourceAcc.getBalance());
        assertEquals(new BigDecimal("225.00"), targetAcc.getBalance());
        assertNotNull(response);

        // Verify that the repository's save method was actually called
        verify(accountRepository, times(2)).save(any(Account.class));
    }

    @Test
    void testTransactionNegativeMoney()
    {
        BigDecimal negativeAmount = new BigDecimal("-50.00");
        // setup
        Account account = new Account();
        account.setAccountNumber("101");
        account.setBalance(new BigDecimal("500.00"));
        DepositRequest depositRequest = new DepositRequest(negativeAmount,
                account.getAccountNumber(),
                "Test Deposit"
        );
        WithdrawRequest withdrawRequest = new WithdrawRequest(negativeAmount,
                account.getAccountNumber(),
                "Test Withdraw"
        );

        // action
        assertThrows(InvalidRequestException.class,() -> transactionsService.depositToAccount(depositRequest));
        assertThrows(InvalidRequestException.class,() -> transactionsService.withdrawFromAccount(withdrawRequest));
    }

    @Test
    void testTransactionGhostAccount()
    {
        // setup
        DepositRequest depositRequest = new DepositRequest(new BigDecimal(100),
                "101",
                "Test Deposit"
        );
        WithdrawRequest withdrawRequest = new WithdrawRequest(new BigDecimal(100),
                "101",
                "Test Withdraw"
        );

        // when mock
        when(accountRepository.findByAccountNumber("101")).thenReturn(Optional.empty());

        // action
        assertThrows(AccountNotFoundException.class,() -> transactionsService.depositToAccount(depositRequest));
        assertThrows(AccountNotFoundException.class,() -> transactionsService.withdrawFromAccount(withdrawRequest));
    }

    @Test
    void testTransferToSelfInvalid()
    {
        // setup
        Account sourceAcc = new Account();
        sourceAcc.setAccountNumber("101");
        sourceAcc.setBalance(new BigDecimal("500.00"));
        Account targetAcc = new Account();
        targetAcc.setAccountNumber("101");
        targetAcc.setBalance(new BigDecimal("100.00"));
        TransferRequest transferRequest = new TransferRequest(new BigDecimal("125.00"),
                sourceAcc.getAccountNumber(),
                targetAcc.getAccountNumber(),
                "Test Transfer"
        );

        // action
        ResponseStatusException responseStatusException = assertThrows(ResponseStatusException.class, () -> transactionsService.transfer(transferRequest));
        assertEquals("Source and Target accounts cannot be the same.", responseStatusException.getReason());
    }

    @Test
    public void testFindTransactionsByAccountNumber()
    {
        // setup
        String accNumber = "101";
        // Create a page request (page 0, size 10)
        Pageable pageable = PageRequest.of(0, 10);

        // when mocks
        // IMPORTANT: Use PageImpl to return a proper Page object
        Page<Transactions> emptyPage = new PageImpl<>(new ArrayList<>());
        when(transactionsRepository.findBySourceAccountOrTargetAccountOrderByTimestampDesc(accNumber,accNumber,pageable))
                .thenReturn((emptyPage));

        // action
        Page<TransactionResponse> result = transactionsService.findTransactionsByAccountNumber(accNumber, pageable);
        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    @Test
    public void testTransfer_SavesTransactionRecord()
    {
        // setup
        Account sourceAcc = new Account();
        sourceAcc.setAccountNumber("101");
        sourceAcc.setBalance(new BigDecimal("50.00"));
        Account targetAcc = new Account();
        targetAcc.setAccountNumber("102");
        targetAcc.setBalance(new BigDecimal("50.00"));
        TransferRequest request = new TransferRequest(new BigDecimal("50.00"), "101", "102", "Transfer");

        // when mocks
        when(accountRepository.findByAccountNumber(eq("101"))).thenReturn(Optional.of(sourceAcc));
        when(accountRepository.findByAccountNumber(eq("102"))).thenReturn(Optional.of(targetAcc));
        when(referenceNumberGenerator.generateUniqueTransactionReference()).thenReturn("REF123456789");
        when(transactionsRepository.save(any(Transactions.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // action (use argumentCaptor before save
        transactionsService.transfer(request);

        ArgumentCaptor<Transactions> captor = ArgumentCaptor.forClass(Transactions.class);
        verify(transactionsRepository, times(1)).save(captor.capture());

        // get data from captor and verify
        Transactions value = captor.getValue();
        assertEquals("REF123456789", value.getTransactionReference());
        assertEquals(new BigDecimal("50.00"), value.getAmount());
    }
}
