package com.nik07roxx.apexPay.DTO.Customer;

import com.nik07roxx.apexPay.model.CustomerStatus;

public record CustomerResponse(Long id,
                               String firstName,
                               String lastName,
                               String email,
                               String phone,
                               String address,
                               CustomerStatus status) {}

