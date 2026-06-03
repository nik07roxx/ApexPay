package com.nik07roxx.apexPay.integration;

import com.nik07roxx.apexPay.ApexPayApplication;
import com.nik07roxx.apexPay.Entity.Account;
import com.nik07roxx.apexPay.Entity.Customer;
import com.nik07roxx.apexPay.Repository.AccountRepository;
import com.nik07roxx.apexPay.Service.Implementation.CurrencyConvertorService;
import com.nik07roxx.apexPay.Service.scheduled.InterestAccrualScheduler;
import com.nik07roxx.apexPay.config.RedisConfig;
import com.nik07roxx.apexPay.model.AccountStatus;
import com.nik07roxx.apexPay.model.AccountType;
import com.nik07roxx.apexPay.model.CurrencyType;
import jakarta.persistence.EntityManager;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = ApexPayApplication.class,
        properties = {
                "spring.kafka.listener.auto-startup=false" // 🌟 Prevents consumers from looking for Kafka
        })
@ActiveProfiles("test")
@Transactional // This rolls back changes after each test so your DB stays clean!
public class SchedulerIntegrationTest {

    @MockBean
    private CurrencyConvertorService currencyConvertorService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private InterestAccrualScheduler interestAccrualScheduler;

    @Autowired // 🌟 INJECT THIS: Resolves the "create a new variable" error
    private EntityManager entityManager;

    @Test
    @DisplayName("Should perform interest accrual on all ACTIVE accounts with any balance")
    void testInterestAccrualOnSchedule() {
        // 1. Arrange: Seed mock records
        Account activeAccount = Account.builder()
                .accountNumber("6141")
                .accountType(AccountType.SAVINGS)
                .balance(new BigDecimal("1000.00"))
                .accountCurrency(CurrencyType.INR)
                .interestRate(new BigDecimal("6.00"))
                .status(AccountStatus.ACTIVE)
                .build();

        Account blockedAccount = Account.builder()
                .accountNumber("6142")
                .accountType(AccountType.SAVINGS)
                .balance(new BigDecimal("1000.00"))
                .accountCurrency(CurrencyType.INR)
                .interestRate(new BigDecimal("6.00"))
                .status(AccountStatus.BLOCKED)
                .build();

        Account noBalAccount = Account.builder()
                .accountNumber("6143")
                .accountType(AccountType.SAVINGS)
                .balance(BigDecimal.ZERO)
                .accountCurrency(CurrencyType.INR)
                .interestRate(new BigDecimal("6.00"))
                .status(AccountStatus.ACTIVE)
                .build();


        accountRepository.save(activeAccount);
        accountRepository.save(blockedAccount);
        accountRepository.save(noBalAccount);

        // 2. Act: Run the cron logic
        interestAccrualScheduler.executeMonthlyInterestAccrual();

        // 🌟 THE FIX: Clear the Hibernate cache so it forces a fresh read from H2
        entityManager.flush();
        entityManager.clear();

        // 3. Assert: Fetch and verify the updated data
        Account activeUpdated = accountRepository.findById(activeAccount.getId()).get();
        Account blockedUpdated = accountRepository.findById(blockedAccount.getId()).get();
        Account noBalUpdated = accountRepository.findById(noBalAccount.getId()).get();

        assertEquals(new BigDecimal("1005.00"), activeUpdated.getBalance());
        assertEquals(new BigDecimal("1000.00"), blockedUpdated.getBalance());
        assertEquals(new BigDecimal("0.00"), noBalUpdated.getBalance());
    }
}
