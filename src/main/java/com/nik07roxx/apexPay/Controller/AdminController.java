package com.nik07roxx.apexPay.Controller;

import com.nik07roxx.apexPay.DTO.Account.AccountBlockRequest;
import com.nik07roxx.apexPay.DTO.Account.AccountBlockResponse;
import com.nik07roxx.apexPay.DTO.Account.AccountResponse;
import com.nik07roxx.apexPay.DTO.Customer.CustomerBlockRequest;
import com.nik07roxx.apexPay.DTO.Customer.CustomerBlockResponse;
import com.nik07roxx.apexPay.DTO.Dashboard.DashboardStatsResponse;
import com.nik07roxx.apexPay.DTO.User.UserViewResponse;
import com.nik07roxx.apexPay.Service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/admin")
@Tag(name = "Administration Management", description = "Endpoints for Admin Tasks")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    @Operation(summary = "List all users in the system")
    public ResponseEntity<Page<UserViewResponse>> findAllUsers(
            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 10, page = 0, sort = "id") Pageable pageable
    )
    {
        return ResponseEntity.ok(adminService.findAllUsers(pageable));
    }

    @GetMapping("/accounts/all")
    @Operation(summary = "List all accounts in the system")
    public ResponseEntity<Page<AccountResponse>> findAllAccounts(
            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 10, page = 0, sort = "id") Pageable pageable
    )
    {
        return ResponseEntity.ok(adminService.findAllAccounts(pageable));
    }

    @PatchMapping("/accounts/{accountRef}/block")
    @Operation(summary = "Block an account")
    public ResponseEntity<AccountBlockResponse> blockAccount(@PathVariable String accountRef,
                                                             @RequestBody AccountBlockRequest request)
    {
        return ResponseEntity.ok(adminService.blockAccount(accountRef,request));
    }

    @PatchMapping("/customer/{id}/block")
    @Operation(summary = "Block a customer and all their accounts")
    public ResponseEntity<CustomerBlockResponse> blockCustomer(@PathVariable Long id,
                                                               @RequestBody CustomerBlockRequest request)
    {
        return ResponseEntity.ok(adminService.blockCustomerAndAllAccounts(id,request));
    }

    @PatchMapping("/users/{id}/promote")
    @Operation(summary = "Promote a user to administrator")
    public ResponseEntity<Void> promoteUserToAdmin(@PathVariable Long id)
    {
        adminService.promoteUserToAdmin(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get system statistics")
    public ResponseEntity<DashboardStatsResponse> getSystemStats()
    {
        return ResponseEntity.ok(adminService.getSystemStats());
    }
}
