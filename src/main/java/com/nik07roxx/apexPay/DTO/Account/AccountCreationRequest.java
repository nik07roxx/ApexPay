package com.nik07roxx.apexPay.DTO.Account;
import com.nik07roxx.apexPay.model.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record AccountCreationRequest(
                            @NotBlank(message = "Account type is required")
                            @Pattern(regexp = "^(SAVINGS|CURRENT)$", message = "Type must be SAVINGS or CURRENT")
                            AccountType accountType,
                            @PositiveOrZero(message = "Initial balance cannot be negative")
                            BigDecimal balance,
                            @NotNull(message = "Customer ID is required")
                            Long customerId) {}
