package com.nik07roxx.apexPay.DTO.Account;
import com.nik07roxx.apexPay.model.AccountType;
import com.nik07roxx.apexPay.model.CurrencyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record AccountUpdationRequest(@NotBlank(message = "Account type is required")
                                     AccountType accountType,
                                     @NotBlank(message = "Balance is required")
                                     BigDecimal balance,
                                     @NotNull
                                     @Pattern(regexp = "^(INR|USD|EUR)$", message = "Currency must be either INR, EUR or USD")
                                     CurrencyType accountCurrency) {}
