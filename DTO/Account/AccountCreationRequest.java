package com.nik07roxx.apexPay.DTO.Account;
import com.nik07roxx.apexPay.model.AccountType;
import java.math.BigDecimal;

public record AccountCreationRequest(AccountType accountType,
                            BigDecimal balance,
                            Long customerId) {}
