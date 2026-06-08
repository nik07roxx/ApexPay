package com.nik07roxx.apexPay.strategy;

import com.nik07roxx.apexPay.DTO.Transactions.StrategyConversionResult;
import com.nik07roxx.apexPay.DTO.Transactions.TransferRequest;
import com.nik07roxx.apexPay.DTO.Transactions.TransactionResponse;
import com.nik07roxx.apexPay.Entity.Account;
import com.nik07roxx.apexPay.Repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Slf4j
@RequiredArgsConstructor
public class SameCurrencyStrategy implements PaymentStrategy{

    @Override
    public StrategyConversionResult processRouting(TransferRequest request,
                                                   Account sourceAccount,
                                                   Account targetAccount) {
        // Logic for finding rate of conversion for same currency transactions
        BigDecimal amount = request.amount();

        log.info("⚡ Strategy Executed: Processing Same-Currency Local Ledger Update");
        return new StrategyConversionResult(new BigDecimal("1.0"), amount, amount);
    }

    @Override
    public String getRoutingType() {
        return "SAME_CURRENCY";
    }
}
