package com.nik07roxx.apexPay.Controller;

import com.nik07roxx.apexPay.DTO.Customer.CustomerCreationRequest;
import com.nik07roxx.apexPay.DTO.Customer.CustomerResponse;
import com.nik07roxx.apexPay.Service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/api/v1/customers")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Customer Onboarding", description = "Endpoints for Customer Opening, Closing, Updation and Views")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @Operation(summary = "Create a new customer with a default account")
    public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CustomerCreationRequest customerCreationRequest)
    {
        CustomerResponse customerResponse = customerService.createCustomer(customerCreationRequest);
        return new ResponseEntity<>(customerResponse, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "List all customers")
    public ResponseEntity<Page<CustomerResponse>> findAllCustomers(
            @Parameter(hidden = true) @PageableDefault(size = 10, page = 0, sort = "id") Pageable pageable,
            @Parameter(description = "Page number", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Size of page", example = "10") @RequestParam(defaultValue = "10") int size
    )
    {
        return ResponseEntity.ok(customerService.findAllCustomers(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Find customer using the Customer Id")
    public ResponseEntity<CustomerResponse> findCustomerById(@PathVariable Long id)
    {
        return ResponseEntity.ok(customerService.findCustomerById(id));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update a customer's details")
    public ResponseEntity<CustomerResponse> updateCustomer(@PathVariable Long id,
                                               @Valid @RequestBody CustomerCreationRequest customerCreationRequest)
    {
        CustomerResponse updatedCustomer = customerService.updateCustomer(id,customerCreationRequest);
        return ResponseEntity.ok(updatedCustomer);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a customer")
    public ResponseEntity<Void> deleteCustomerById(@PathVariable Long id)
    {
        customerService.deleteCustomerById(id);
        return ResponseEntity.noContent().build();
    }
}
