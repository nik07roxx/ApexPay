package com.nik07roxx.apexPay.Service.Implementation;

import com.nik07roxx.apexPay.DTO.Account.AccountBlockRequest;
import com.nik07roxx.apexPay.DTO.Account.AccountBlockResponse;
import com.nik07roxx.apexPay.DTO.Account.AccountResponse;
import com.nik07roxx.apexPay.DTO.Customer.CustomerBlockRequest;
import com.nik07roxx.apexPay.DTO.Customer.CustomerBlockResponse;
import com.nik07roxx.apexPay.DTO.Dashboard.DashboardStatsResponse;
import com.nik07roxx.apexPay.DTO.User.UserViewResponse;
import com.nik07roxx.apexPay.Entity.Account;
import com.nik07roxx.apexPay.Entity.Customer;
import com.nik07roxx.apexPay.Entity.Role;
import com.nik07roxx.apexPay.Entity.User;
import com.nik07roxx.apexPay.Repository.*;
import com.nik07roxx.apexPay.Service.AdminService;
import com.nik07roxx.apexPay.exceptions.AccountNotFoundException;
import com.nik07roxx.apexPay.exceptions.CustomerNotFoundException;
import com.nik07roxx.apexPay.exceptions.UserNotFoundException;
import com.nik07roxx.apexPay.model.AccountStatus;
import com.nik07roxx.apexPay.model.CustomerStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionsRepository transactionsRepository;
    private final RoleRepository roleRepository;

    @Override
    public Page<UserViewResponse> findAllUsers(Pageable pageable) {
        log.info("Finding all users in the system.");
        Page<User> allUsers = userRepository.findAll(pageable);
        return allUsers.map(user -> new UserViewResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail())
        );
    }

    @Override
    public Page<AccountResponse> findAllAccounts(Pageable pageable) {
        log.info("Finding all accounts in the system.");
        Page<Account> allAccounts = accountRepository.findAll(pageable);
        return allAccounts.map(account -> new AccountResponse(
                account.getAccountNumber(),
                account.getAccountType(),
                account.getBalance(),
                account.getInterestRate(),
                account.getOpeningDate(),
                account.getStatus())
        );
    }

    @Override
    @Transactional
    public AccountBlockResponse blockAccount(String accountNumber, AccountBlockRequest request) {
        // check if account exists, and find it
        log.info("Finding account with account number {} for blocking.", accountNumber);
        Account foundAccount = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> {
                    log.error("No account found with account number: {}", accountNumber);
                    return new AccountNotFoundException("Account not found: " + accountNumber);
                });

        // change status and save the account
        foundAccount.setStatus(AccountStatus.BLOCKED);
        log.info("Marking account number {} as BLOCKED, by admin for reason: {}.", accountNumber, request.reason());
        Account savedAccount = accountRepository.save(foundAccount);

        // populate response and return
        AccountBlockResponse response = new AccountBlockResponse(
                savedAccount.getAccountNumber(),
                savedAccount.getAccountType(),
                savedAccount.getStatus(),
                request.reason()
        );
        return response;
    }

    @Override
    @Transactional
    public CustomerBlockResponse blockCustomerAndAllAccounts(Long customerId, CustomerBlockRequest request) {
        // check if customer exists, and find it
        log.info("Finding customer with id {} for blocking.", customerId);
        Customer foundCustomer = customerRepository.findById(customerId)
                .orElseThrow(() -> {
                    log.error("No customer found with id: {}", customerId);
                    return new CustomerNotFoundException("Customer not found: " + customerId);
                });

        // change status
        foundCustomer.setStatus(CustomerStatus.BLOCKED);
        log.info("Marking customer id {} as BLOCKED, by admin for reason: {}.", customerId, request.reason());

        // find all accounts linked to customer and mark them as blocked
        foundCustomer.getAccounts()
                .forEach(account -> {
                    log.info("Marking account number {} as BLOCKED, by admin for reason: {}.", account.getAccountNumber(), request.reason());
                    account.setStatus(AccountStatus.BLOCKED);
                });

        // save the customer
        Customer savedCustomer = customerRepository.save(foundCustomer);

        // populate response and return
        CustomerBlockResponse response = new CustomerBlockResponse(
                savedCustomer.getId(),
                savedCustomer.getFirstName(),
                savedCustomer.getEmail(),
                savedCustomer.getStatus(),
                request.reason()
        );
        return response;
    }

    @Override
    @Transactional
    public void promoteUserToAdmin(Long userId) {
        log.info("Finding user with id {} for promotion to admin.", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("No user found with id: {}", userId);
                    return new UserNotFoundException("User not found: " + userId);
                });

        // FETCH the existing role from your RoleRepository
        log.info("Finding the ADMIN role in the Role table.");
        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> {
                    log.error("No ADMIN role found in Role table.");
                    return new RuntimeException("No ADMIN role found in Role table.");
                });

        user.getRoles().add(adminRole);
        log.info("Saving user with id {} with ADMIN role.", userId);
        userRepository.save(user);
    }

    @Override
    public DashboardStatsResponse getSystemStats() {
        return new DashboardStatsResponse(
                userRepository.count(),
                customerRepository.count(),
                accountRepository.sumAllBalances() // Make sure you have this in AccountRepository
        );
    }
}
