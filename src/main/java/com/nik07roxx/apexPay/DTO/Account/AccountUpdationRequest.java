package com.nik07roxx.apexPay.DTO.Account;
import com.nik07roxx.apexPay.model.AccountType;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record AccountUpdationRequest(
                                     @NotBlank(message = "Account type is required")
                                     AccountType accountType,
                                     @NotBlank(message = "Balance is required")
                                     BigDecimal balance) {}
