package com.nik07roxx.apexPay.DTO.Customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CustomerCreationRequest(
                                        @NotBlank(message = "First name is required")
                                        String firstName,
                                        @NotBlank(message = "Last name is required")
                                        String lastName,
                                        @Email(message = "Invalid email format")
                                        @NotBlank(message = "Email is required")
                                        String email,
                                        @NotBlank(message = "Phone number is required")
                                        @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
                                        String phone,
                                        @NotBlank(message = "Address is required")
                                        String address) {}
