package com.nik07roxx.apexPay.Service;

import com.nik07roxx.apexPay.DTO.Currency.CurrencyConvertorResponse;
import com.nik07roxx.apexPay.DTO.Transactions.DepositRequest;
import com.nik07roxx.apexPay.DTO.Transactions.TransactionResponse;
import com.nik07roxx.apexPay.DTO.Transactions.TransferRequest;
import com.nik07roxx.apexPay.DTO.Transactions.WithdrawRequest;
import com.nik07roxx.apexPay.Entity.Account;
import com.nik07roxx.apexPay.Entity.Transactions;
import com.nik07roxx.apexPay.Repository.AccountRepository;
import com.nik07roxx.apexPay.Repository.TransactionsRepository;
import com.nik07roxx.apexPay.Service.Implementation.CurrencyConvertorService;
import com.nik07roxx.apexPay.Service.Implementation.TransactionsServiceImpl;
import com.nik07roxx.apexPay.exceptions.AccountNotFoundException;
import com.nik07roxx.apexPay.exceptions.InsufficientFundsException;
import com.nik07roxx.apexPay.exceptions.InvalidRequestException;
import com.nik07roxx.apexPay.model.CurrencyType;
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
import java.util.*;

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

    @Mock
    private CurrencyConvertorService currencyConvertorService;

    @Test
    void testSameSourceAndTargetCurrencyTransferSuccess()
    {
        // Setup
        Account sourceAcc = new Account();
        sourceAcc.setAccountNumber("101");
        sourceAcc.setBalance(new BigDecimal("500.00"));
        sourceAcc.setAccountCurrency(CurrencyType.INR);

        Account targetAcc = new Account();
        targetAcc.setAccountNumber("102");
        targetAcc.setBalance(new BigDecimal("100.00"));
        targetAcc.setAccountCurrency(CurrencyType.INR);

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

        assertEquals(0, new BigDecimal("375.00").compareTo(sourceAcc.getBalance()));
        assertEquals(0, new BigDecimal("225.00").compareTo(targetAcc.getBalance()));
        assertNotNull(response);

        // Verify that the repository's save method was actually called
        verify(accountRepository).save(sourceAcc);
        verify(accountRepository).save(targetAcc);
    }

    @Test
    void testDifferentSourceAndTargetCurrencyTransferSuccess()
    {
        // Setup
        Account sourceAcc = new Account();
        sourceAcc.setAccountNumber("101");
        sourceAcc.setBalance(new BigDecimal("500.00"));
        sourceAcc.setAccountCurrency(CurrencyType.USD);

        Account targetAcc = new Account();
        targetAcc.setAccountNumber("102");
        targetAcc.setBalance(new BigDecimal("100.00"));
        targetAcc.setAccountCurrency(CurrencyType.INR);

        TransferRequest transferRequest = new TransferRequest(new BigDecimal("125.00"),
                sourceAcc.getAccountNumber(),
                targetAcc.getAccountNumber(),
                "Test Foreign Currency Transfer"
        );

        Map<String, BigDecimal> rates = new HashMap<>();
        rates.put("USD", new BigDecimal("1.0"));
        rates.put("INR", new BigDecimal("83.50"));
        rates.put("EUR", new BigDecimal("0.92"));

        CurrencyConvertorResponse mockApiResponse = new CurrencyConvertorResponse();
        mockApiResponse.setResult("success");
        mockApiResponse.setBaseCode("USD");
        mockApiResponse.setConversionRates(rates);
        mockApiResponse.setTimeLastUpdateUnix(1715623600);
        mockApiResponse.setTimeLastUpdateUtc("Wed, 13 May 2026 18:00:00 +0000");

        // Act
        when(accountRepository.findByAccountNumber("101")).thenReturn(Optional.of(sourceAcc));
        when(accountRepository.findByAccountNumber("102")).thenReturn(Optional.of(targetAcc));
        when(referenceNumberGenerator.generateUniqueTransactionReference()).thenReturn("REF12345");
        when(transactionsRepository.save(any(Transactions.class))).thenAnswer(i -> i.getArguments()[0]);
        when(currencyConvertorService.getConvertedCurrencyAmount(sourceAcc.getAccountCurrency())).thenReturn(mockApiResponse);

        TransactionResponse response = transactionsService.transfer(transferRequest);

        assertEquals(0, new BigDecimal("375.00").compareTo(sourceAcc.getBalance()));
        assertEquals(0, new BigDecimal("10537.50").compareTo(targetAcc.getBalance()));
        assertNotNull(response);

        // Verify that the repository's save method was actually called
        verify(accountRepository).save(sourceAcc);
        verify(accountRepository).save(targetAcc);
    }

    @Test
    void testTransactionNegativeMoney()
    {
        BigDecimal negativeAmount = new BigDecimal("-50.00");
        // setup
        Account account = new Account();
        account.setAccountNumber("101");
        account.setBalance(new BigDecimal("500.00"));
        account.setAccountCurrency(CurrencyType.EUR);

        Account account2 = new Account();
        account2.setAccountNumber("102");
        account2.setBalance(new BigDecimal("500.00"));
        account2.setAccountCurrency(CurrencyType.EUR);

        DepositRequest depositRequest = new DepositRequest(negativeAmount,
                account.getAccountNumber(),
                "Test Deposit"
        );
        WithdrawRequest withdrawRequest = new WithdrawRequest(negativeAmount,
                account.getAccountNumber(),
                "Test Withdraw"
        );
        TransferRequest transferRequest = new TransferRequest(negativeAmount,
                account.getAccountNumber(),
                account2.getAccountNumber(),
                "Test Transfer"
        );

        // action
        InvalidRequestException invalidRequestException_deposit =
                assertThrows(InvalidRequestException.class,
                        () -> transactionsService.depositToAccount(depositRequest));
        InvalidRequestException invalidRequestException_withdraw =
                assertThrows(InvalidRequestException.class,
                        () -> transactionsService.withdrawFromAccount(withdrawRequest));
        InvalidRequestException invalidRequestException_transfer =
                assertThrows(InvalidRequestException.class,
                        () -> transactionsService.transfer(transferRequest));


        assertEquals("Deposit amount cannot be negative or zero.",
                invalidRequestException_deposit.getMessage());
        assertEquals("Withdraw amount cannot be negative or zero.",
                invalidRequestException_withdraw.getMessage());
        assertEquals("Transfer amount cannot be negative or zero.",
                invalidRequestException_transfer.getMessage());
    }

    @Test
    void testTransactionGhostAccount()
    {
        // setup
        Account account = new Account();
        account.setAccountNumber("101");
        account.setBalance(new BigDecimal("500.00"));
        account.setAccountCurrency(CurrencyType.EUR);

        DepositRequest depositRequest = new DepositRequest(new BigDecimal(100),
                "102",
                "Test Deposit"
        );
        WithdrawRequest withdrawRequest = new WithdrawRequest(new BigDecimal(100),
                "102",
                "Test Withdraw"
        );
        TransferRequest transferRequest1 = new TransferRequest(new BigDecimal(100),
                "101",
                "102",
                "Test Transfer"
        );
        TransferRequest transferRequest2 = new TransferRequest(new BigDecimal(100),
                "102",
                "101",
                "Test Transfer 2"
        );

        // when mock
        when(accountRepository.findByAccountNumber("101")).thenReturn(Optional.of(account));
        when(accountRepository.findByAccountNumber("102")).thenReturn(Optional.empty());

        // action
        AccountNotFoundException depositANFException = assertThrows(AccountNotFoundException.class, () -> transactionsService.depositToAccount(depositRequest));
        AccountNotFoundException withdrawANFException = assertThrows(AccountNotFoundException.class, () -> transactionsService.withdrawFromAccount(withdrawRequest));
        AccountNotFoundException transferANFException1 = assertThrows(AccountNotFoundException.class, () -> transactionsService.transfer(transferRequest1));
        AccountNotFoundException transferANFException2 = assertThrows(AccountNotFoundException.class, () -> transactionsService.transfer(transferRequest2));

        assertEquals("Account not found: 102",
                depositANFException.getMessage());
        assertEquals("Account not found: 102",
                withdrawANFException.getMessage());
        assertEquals("Account not found: 102",
                transferANFException1.getMessage());
        assertEquals("Account not found: 102",
                transferANFException2.getMessage());

        assertEquals(0,new BigDecimal("500.00").compareTo(account.getBalance()));

        verify(accountRepository,times(0)).save(any(Account.class));
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
        sourceAcc.setAccountCurrency(CurrencyType.INR);
        Account targetAcc = new Account();
        targetAcc.setAccountNumber("102");
        targetAcc.setBalance(new BigDecimal("50.00"));
        targetAcc.setAccountCurrency(CurrencyType.INR);
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
        assertEquals(new BigDecimal("50.00"), value.getSourceAmount());
    }

    @Test
    public void testInsufficientFundsInSourceAccountCancelsTransfer()
    {
        // setup
        Account account = new Account();
        account.setAccountNumber("101");
        account.setBalance(new BigDecimal("500.00"));
        account.setAccountCurrency(CurrencyType.EUR);

        Account account2 = new Account();
        account2.setAccountNumber("102");
        account2.setBalance(new BigDecimal("500.00"));
        account2.setAccountCurrency(CurrencyType.EUR);

        TransferRequest transferRequest = new TransferRequest(new BigDecimal("1000.00"),
                account.getAccountNumber(),
                account2.getAccountNumber(),
                "Test Transfer"
        );

        // when mocks
        when(accountRepository.findByAccountNumber("101")).thenReturn(Optional.of(account));

        // action
        InsufficientFundsException insufficientFundsException =
                assertThrows(InsufficientFundsException.class,
                        () -> transactionsService.transfer(transferRequest));

        assertEquals("Source Account has less balance than transfer request.",
                insufficientFundsException.getMessage() );
    }
}
