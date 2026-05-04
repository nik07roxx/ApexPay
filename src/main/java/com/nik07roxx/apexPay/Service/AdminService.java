package com.nik07roxx.apexPay.Service;

import com.nik07roxx.apexPay.DTO.Account.AccountBlockRequest;
import com.nik07roxx.apexPay.DTO.Account.AccountBlockResponse;
import com.nik07roxx.apexPay.DTO.Account.AccountResponse;
import com.nik07roxx.apexPay.DTO.Customer.CustomerBlockRequest;
import com.nik07roxx.apexPay.DTO.Customer.CustomerBlockResponse;
import com.nik07roxx.apexPay.DTO.Customer.CustomerResponse;
import com.nik07roxx.apexPay.DTO.Dashboard.DashboardStatsResponse;
import com.nik07roxx.apexPay.DTO.User.UserViewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminService {
    // User and Account Oversight
    Page<UserViewResponse> findAllUsers(Pageable pageable);
    Page<AccountResponse> findAllAccounts(Pageable pageable);
    AccountBlockResponse blockAccount(String accountNumber, AccountBlockRequest request);
    CustomerBlockResponse blockCustomerAndAllAccounts(Long customerId, CustomerBlockRequest request);
    void promoteUserToAdmin(Long userId);
    DashboardStatsResponse getSystemStats();
}
