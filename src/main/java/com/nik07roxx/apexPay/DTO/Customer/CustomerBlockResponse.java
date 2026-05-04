package com.nik07roxx.apexPay.DTO.Customer;

import com.nik07roxx.apexPay.model.CustomerStatus;

public record CustomerBlockResponse(Long id,
                                    String firstName,
                                    String email,
                                    CustomerStatus status,
                                    String reason) {}