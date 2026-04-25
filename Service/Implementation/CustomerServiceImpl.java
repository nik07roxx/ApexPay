package com.nik07roxx.apexPay.Service.Implementation;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.nik07roxx.apexPay.DTO.Account.AccountCreationRequest;
import com.nik07roxx.apexPay.DTO.Account.AccountResponse;
import com.nik07roxx.apexPay.DTO.Customer.CustomerCreationRequest;
import com.nik07roxx.apexPay.DTO.Customer.CustomerResponse;
import com.nik07roxx.apexPay.Entity.Account;
import com.nik07roxx.apexPay.Entity.Customer;
import com.nik07roxx.apexPay.Repository.AccountRepository;
import com.nik07roxx.apexPay.Repository.CustomerRepository;
import com.nik07roxx.apexPay.Service.AccountService;
import com.nik07roxx.apexPay.Service.CustomerService;
import com.nik07roxx.apexPay.model.AccountStatus;
import com.nik07roxx.apexPay.model.AccountType;
import com.nik07roxx.apexPay.model.CustomerStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;
    private final AccountService accountService;

    @Override
    @Transactional
    public CustomerResponse createCustomer(CustomerCreationRequest customerCreationRequest)
    {
        // check if email already exists in DB
        if(customerRepository.existsByEmail(customerCreationRequest.email()))
        {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Customer with this email already exists.");
        }

        // save the customer
        Customer customer = new Customer();
        customer.setFirstName(customerCreationRequest.firstName());
        customer.setLastName(customerCreationRequest.lastName());
        customer.setEmail(customerCreationRequest.email());
        customer.setAddress(customerCreationRequest.address());
        customer.setPhone(customerCreationRequest.phone());
        customer.setStatus(CustomerStatus.ACTIVE);

        Customer savedCustomer = customerRepository.save(customer);

        // create a default account for the customer
        AccountResponse defaultAccount = accountService.createAccount(new AccountCreationRequest(
                                                                AccountType.CURRENT,
                                                                BigDecimal.ZERO,
                                                                savedCustomer.getId()));

        // return the response
        CustomerResponse customerResponse = new CustomerResponse(
                savedCustomer.getId(),
                savedCustomer.getFirstName(),
                savedCustomer.getLastName(),
                savedCustomer.getEmail(),
                savedCustomer.getPhone(),
                savedCustomer.getAddress(),
                savedCustomer.getStatus()
        );
        return customerResponse;
    }

    @Override
    public List<CustomerResponse> findAllCustomers() {
        // retrieve all customers from DB
        List<Customer> customers= customerRepository.findByStatus(CustomerStatus.ACTIVE);
        List<CustomerResponse> cleanedCustomers = new ArrayList<>();

        // insert all customers into a customer response list in a loop
        for(Customer currentCustomer: customers)
        {
            CustomerResponse customerResponse = new CustomerResponse(
                    currentCustomer.getId(),
                    currentCustomer.getFirstName(),
                    currentCustomer.getLastName(),
                    currentCustomer.getEmail(),
                    currentCustomer.getPhone(),
                    currentCustomer.getAddress(),
                    currentCustomer.getStatus()
            );
            cleanedCustomers.add(customerResponse);
        }
        return cleanedCustomers;
    }

    @Override
    public CustomerResponse findCustomerById(Long id) {
        Customer currentCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "No customer found with id: "+id));
        CustomerResponse customerResponse = new CustomerResponse(
                currentCustomer.getId(),
                currentCustomer.getFirstName(),
                currentCustomer.getLastName(),
                currentCustomer.getEmail(),
                currentCustomer.getPhone(),
                currentCustomer.getAddress(),
                currentCustomer.getStatus()
        );
        return customerResponse;
    }

    @Override
    @Transactional
    public CustomerResponse updateCustomer(Long id, CustomerCreationRequest customerCreationRequest) {
        // validate if customer exists
        Customer currentCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "No customer found with id: "+id));

        if(customerCreationRequest.firstName() != null)
            currentCustomer.setFirstName(customerCreationRequest.firstName());
        if(customerCreationRequest.lastName() != null)
            currentCustomer.setLastName(customerCreationRequest.lastName());
        if(customerCreationRequest.email() != null)
            currentCustomer.setEmail(customerCreationRequest.email());
        if(customerCreationRequest.phone() != null)
            currentCustomer.setPhone(customerCreationRequest.phone());
        if(customerCreationRequest.address() != null)
            currentCustomer.setAddress(customerCreationRequest.address());

        Customer savedCustomer = customerRepository.save(currentCustomer);

        CustomerResponse customerResponse = new CustomerResponse(
                savedCustomer.getId(),
                savedCustomer.getFirstName(),
                savedCustomer.getLastName(),
                savedCustomer.getEmail(),
                savedCustomer.getPhone(),
                savedCustomer.getAddress(),
                savedCustomer.getStatus()
        );
        return customerResponse;
    }

    @Override
    @Transactional
    public void deleteCustomerById(Long id) {
        // check if customer exists
        Customer savedCustomer = customerRepository.findById(id)
                        .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "No customer found with id: "+id));

        // check if customer has any account with money, or debt, before closing
        if(savedCustomer.getAccounts().stream()
                .anyMatch(a -> a.getBalance().compareTo(BigDecimal.ZERO) != 0))
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot close customer with active balances");
        }

        // close all their accounts first
        savedCustomer.getAccounts()
                .forEach(account -> account.setStatus(AccountStatus.CLOSED));

        // close the customer
        savedCustomer.setStatus(CustomerStatus.CLOSED);
        customerRepository.save(savedCustomer);
    }
}