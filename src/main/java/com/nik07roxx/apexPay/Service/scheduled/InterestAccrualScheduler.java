package com.nik07roxx.apexPay.Service.scheduled;

import com.nik07roxx.apexPay.Repository.AccountRepository;
import com.nik07roxx.apexPay.model.AccountStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class InterestAccrualScheduler {

    private final AccountRepository accountRepository;

    // Runs at 11:00 PM on the last day of every month
    @Scheduled(cron = "0 0 23 L * ?")
    public void executeMonthlyInterestAccrual() {
        log.info("EOM Cron Triggered: Initializing global monthly interest accrual processing...");

        long startTime = System.currentTimeMillis();
        int updatedRows = accountRepository.applyMonthlyInterestExcludingZeroBalances(AccountStatus.ACTIVE);
        long duration = System.currentTimeMillis() - startTime;

        log.info("EOM Interest Accrual completed successfully. Total accounts processed: {}. " +
                "Time taken: {}ms", updatedRows, duration);
    }
}
