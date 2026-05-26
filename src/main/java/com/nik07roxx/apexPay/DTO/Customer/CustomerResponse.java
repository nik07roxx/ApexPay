package com.nik07roxx.apexPay.DTO.Customer;
import java.io.Serializable;
import com.nik07roxx.apexPay.model.CustomerStatus;

public record CustomerResponse(Long id,
                               String firstName,
                               String lastName,
                               String email,
                               String phone,
                               String address,
                               CustomerStatus status) implements Serializable {
                                private static final long serialVersionUID = 1L;
                            }

