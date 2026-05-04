package com.nik07roxx.apexPay.Repository;

import com.nik07roxx.apexPay.Entity.Account;
import com.nik07roxx.apexPay.model.AccountStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account,Long> {
    boolean existsByAccountNumber(String accountNumber);
    Optional<Account> findByAccountNumber(String accountNumber);
    Page<Account> findByStatus(AccountStatus accountStatus, Pageable pageable);
    Page<Account> findByCustomerId(Long customerId, Pageable pageable);

    @Query("SELECT SUM(a.balance) FROM Account a")
    BigDecimal sumAllBalances();
}
