package com.nik07roxx.apexPay.Controller;

import com.nik07roxx.apexPay.DTO.Customer.CustomerCreationRequest;
import com.nik07roxx.apexPay.DTO.Customer.CustomerResponse;
import com.nik07roxx.apexPay.Service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
    @RequestMapping("/api/v1/customers")
public class CustomerController {

    private CustomerService customerService;

    @Autowired
    public CustomerController(CustomerService customerService)
    {
        this.customerService = customerService;
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CustomerCreationRequest customerCreationRequest)
    {
        CustomerResponse customerResponse = customerService.createCustomer(customerCreationRequest);
        return new ResponseEntity<>(customerResponse, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponse>> findAllCustomers()
    {
        return ResponseEntity.ok(customerService.findAllCustomers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> findCustomerById(@PathVariable Long id)
    {
        return ResponseEntity.ok(customerService.findCustomerById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CustomerResponse> updateCustomer(@PathVariable Long id,
                                               @Valid @RequestBody CustomerCreationRequest customerCreationRequest)
    {
        CustomerResponse updatedCustomer = customerService.updateCustomer(id,customerCreationRequest);
        return ResponseEntity.ok(updatedCustomer);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomerById(@PathVariable Long id)
    {
        customerService.deleteCustomerById(id);
        return ResponseEntity.noContent().build();
    }
}
