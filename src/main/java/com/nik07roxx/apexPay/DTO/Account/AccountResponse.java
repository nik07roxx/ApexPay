package com.nik07roxx.apexPay.DTO.Account;
import com.nik07roxx.apexPay.model.AccountStatus;
import com.nik07roxx.apexPay.model.AccountType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountResponse(String accountNumber,
                              AccountType accountType,
                              BigDecimal balance,
                              BigDecimal interestRate,
                              LocalDateTime openingDate,
                              AccountStatus status) {}


