package com.nik07roxx.apexPay.Service;

import com.nik07roxx.apexPay.DTO.Account.AccountCreationRequest;
import com.nik07roxx.apexPay.DTO.Account.AccountResponse;
import com.nik07roxx.apexPay.Entity.Account;
import com.nik07roxx.apexPay.Entity.Customer;
import com.nik07roxx.apexPay.Repository.AccountRepository;
import com.nik07roxx.apexPay.Repository.CustomerRepository;
import com.nik07roxx.apexPay.Service.Implementation.AccountServiceImpl;
import com.nik07roxx.apexPay.exceptions.AccountNotFoundException;
import com.nik07roxx.apexPay.model.AccountType;
import com.nik07roxx.apexPay.model.CustomerStatus;
import com.nik07roxx.apexPay.util.ReferenceNumberGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTests {

    @InjectMocks
    private AccountServiceImpl accountService;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ReferenceNumberGenerator referenceNumberGenerator;

    @Test
    public void successfulAccountCreationTest()
    {
        // setup
        AccountCreationRequest accountCreationRequest = new AccountCreationRequest(
                AccountType.CURRENT,
                new BigDecimal("55.00"),
                1L
        );

        Customer customer = new Customer();
        customer.setId(1L);
        customer.setFirstName("Nikhil");
        customer.setEmail("bn@gmail.com");
        customer.setAccounts(new ArrayList<>());

        // when mocks
        when(customerRepository.findById(eq(1L))).thenReturn(Optional.of(customer));
        when(referenceNumberGenerator.generateUniqueAccountNumber()).thenReturn("NEWREFTEST");
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // action
        AccountResponse accountResponse = accountService.createAccount(accountCreationRequest);

        // verify
        assertEquals("NEWREFTEST",accountResponse.accountNumber());
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    public void findAccountByAccountNumberThrowsAccountNotFoundException()
    {
        // setup
        String accNum = "123";

        // when mocks
        // none needed

        // verify
        assertThrows(AccountNotFoundException.class,() -> accountService.findAccountByAccountNumber(accNum));
    }
}
