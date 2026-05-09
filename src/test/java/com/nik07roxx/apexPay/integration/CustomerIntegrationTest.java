package com.nik07roxx.apexPay.integration;

import com.nik07roxx.apexPay.ApexPayApplication;
import com.nik07roxx.apexPay.DTO.Customer.CustomerCreationRequest;
import com.nik07roxx.apexPay.DTO.Customer.CustomerResponse;
import com.nik07roxx.apexPay.Entity.Account;
import com.nik07roxx.apexPay.Entity.Customer;
import com.nik07roxx.apexPay.Repository.AccountRepository;
import com.nik07roxx.apexPay.Repository.CustomerRepository;
import com.nik07roxx.apexPay.Service.CustomerService;
import com.nik07roxx.apexPay.model.AccountType;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ApexPayApplication.class)
@ActiveProfiles("test")
@Transactional // This rolls back changes after each test so your DB stays clean!
public class CustomerIntegrationTest {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Test
    @DisplayName("Should save customer and create a default account automatically")
    void testSaveCustomerCreatesDefaultAccount() {
        // Act: Call the real service
        CustomerResponse customerResponse = customerService.createCustomer(
                new CustomerCreationRequest(
                      "Nityanand",
                      "Bhandary",
                      "nitya289@gmail.com",
                      "9029068426",
                      "Mira Road"
                )
        );

        // Assert: Verify the data reached the database
        assertNotNull(customerResponse.id());

        // Check if the account was actually created in the database
        Pageable pageable = PageRequest.of(0,5);
        Page<Account> allAccountsForCustomer = accountRepository.findByCustomerId(customerResponse.id(), pageable);
        assertTrue(!allAccountsForCustomer.isEmpty(), "Default account should have been created");
        assertEquals(AccountType.CURRENT, allAccountsForCustomer.getContent().get(0).getAccountType());
    }
}
