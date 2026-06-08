package com.nik07roxx.apexPay.DTO.Customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CustomerUpdationRequest(  String firstName,
                                        String lastName,
                                        @Email(message = "Invalid email format")
                                        String email,
                                        @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
                                        String phone,
                                        String address) {}
