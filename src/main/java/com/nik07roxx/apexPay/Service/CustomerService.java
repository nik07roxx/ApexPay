package com.nik07roxx.apexPay.Service;

import com.nik07roxx.apexPay.DTO.Customer.CustomerCreationRequest;
import com.nik07roxx.apexPay.DTO.Customer.CustomerResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CustomerService {
    CustomerResponse createCustomer(CustomerCreationRequest customerCreationRequest);
    Page<CustomerResponse> findAllCustomers(Pageable pageable);
    CustomerResponse findCustomerById(Long id);
    CustomerResponse updateCustomer(Long id, CustomerCreationRequest customerCreationRequest);
    void deleteCustomerById(Long id);
}
