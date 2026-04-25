package com.nik07roxx.apexPay.DTO.Customer;

public record CustomerCreationRequest(String firstName,
                                      String lastName,
                                      String email,
                                      String phone,
                                      String address) {}
