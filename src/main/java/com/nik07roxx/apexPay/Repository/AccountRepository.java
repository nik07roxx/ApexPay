package com.nik07roxx.apexPay.Repository;

import com.nik07roxx.apexPay.Entity.Account;
import com.nik07roxx.apexPay.model.AccountStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account,Long> {
    boolean existsByAccountNumber(String accountNumber);
    Page<Account> findByStatus(AccountStatus accountStatus, Pageable pageable);
    Page<Account> findByCustomerId(Long customerId, Pageable pageable);

    @Query("SELECT SUM(a.balance) FROM Account a")
    BigDecimal sumAllBalances();

    @Query("SELECT a from Account a join fetch a.customer where a.accountNumber = :accountNumber")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Account> findByAccountNumber(@Param("accountNumber") String accountNumber);

    @Query("SELECT a from Account a where a.accountNumber = :accountNumber")
    Optional<Account> findByAccountNumberForRead(@Param("accountNumber") String accountNumber);

    @Modifying
    @Transactional
    @Query("UPDATE Account a SET a.balance = a.balance + (a.balance * (a.interestRate / 12.0 / 100.0)) " +
            "WHERE a.balance > 0 AND a.status = :status")
    int applyMonthlyInterestExcludingZeroBalances(@Param("status") AccountStatus status);
}
