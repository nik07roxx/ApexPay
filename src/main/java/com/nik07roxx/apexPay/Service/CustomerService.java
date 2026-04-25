package com.nik07roxx.apexPay.Service;

import com.nik07roxx.apexPay.DTO.Customer.CustomerCreationRequest;
import com.nik07roxx.apexPay.DTO.Customer.CustomerResponse;

import java.util.List;

public interface CustomerService {
    CustomerResponse createCustomer(CustomerCreationRequest customerCreationRequest);
    List<CustomerResponse> findAllCustomers();
    CustomerResponse findCustomerById(Long id);
    CustomerResponse updateCustomer(Long id, CustomerCreationRequest customerCreationRequest);
    void deleteCustomerById(Long id);
}
