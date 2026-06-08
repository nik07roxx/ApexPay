package com.nik07roxx.apexPay.strategy;

import com.nik07roxx.apexPay.DTO.Currency.CurrencyConvertorResponse;
import com.nik07roxx.apexPay.DTO.Transactions.StrategyConversionResult;
import com.nik07roxx.apexPay.DTO.Transactions.TransferRequest;
import com.nik07roxx.apexPay.DTO.Transactions.TransactionResponse;
import com.nik07roxx.apexPay.Entity.Account;
import com.nik07roxx.apexPay.Service.Implementation.CurrencyConvertorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Slf4j
@RequiredArgsConstructor
public class CrossCurrencyStrategy implements PaymentStrategy{

    private final CurrencyConvertorService currencyConvertorService;

    @Override
    public StrategyConversionResult processRouting(TransferRequest request,
                                                   Account sourceAccount,
                                                   Account targetAccount) {
        // Logic for finding rate of conversion for foreign currency transactions
        CurrencyConvertorResponse convertedCurrencyAmount = currencyConvertorService
                .getConvertedCurrencyAmount(sourceAccount.getAccountCurrency());

        BigDecimal rate = convertedCurrencyAmount.getRate(targetAccount.getAccountCurrency().toString());
        BigDecimal debitAmount = request.amount();
        BigDecimal creditAmount = debitAmount.multiply(rate);
        log.info("🌎 Strategy Executed: Processing Cross-Currency FX Conversion Engine");
        return new StrategyConversionResult(rate, debitAmount, creditAmount);
    }

    @Override
    public String getRoutingType() {
        return "CROSS_CURRENCY";
    }
}