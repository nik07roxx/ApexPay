package com.nik07roxx.apexPay.util;

import com.nik07roxx.apexPay.Repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class ReferenceNumberGenerator {
    private final AccountRepository accountRepository;

    public String generateUniqueAccountNumber() {
        // Your logic here (Random, Sequence-based, etc.)
        int length = 10;
        // Using ThreadLocalRandom is more efficient in Spring services
        long min = (long) Math.pow(10, length - 1); // 1,000,000,000
        long max = (long) Math.pow(10, length) - 1; // 9,999,999,999

        long result = ThreadLocalRandom.current().nextLong(min, max + 1);
        String accountNumber = Long.toString(result);

        // If by some miracle this exists, just call yourself again!
        if (accountRepository.existsByAccountNumber(accountNumber)) {
            return generateUniqueAccountNumber();
        }
        return accountNumber;
    }

    public String generateUniqueTransactionReference()
    {
        return UUID.randomUUID().toString();
    }
}