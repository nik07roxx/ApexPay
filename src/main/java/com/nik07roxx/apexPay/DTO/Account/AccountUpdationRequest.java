package com.nik07roxx.apexPay.DTO.Account;
import com.nik07roxx.apexPay.model.AccountType;
import java.math.BigDecimal;

public record AccountUpdationRequest(AccountType accountType,
                                     BigDecimal balance) {}
