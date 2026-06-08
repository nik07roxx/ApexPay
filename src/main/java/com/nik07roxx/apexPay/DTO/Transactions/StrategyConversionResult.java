package com.nik07roxx.apexPay.DTO.Transactions;

import java.math.BigDecimal;

public record StrategyConversionResult (BigDecimal rateOfConversion,
                                        BigDecimal debitAmount,
                                        BigDecimal creditAmount){}
