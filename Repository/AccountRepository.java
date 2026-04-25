package com.nik07roxx.apexPay.Repository;

import com.nik07roxx.apexPay.Entity.Account;
import com.nik07roxx.apexPay.model.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account,Long> {
    boolean existsByAccountNumber(String accountNumber);
    Optional<Account> findByAccountNumber(String accountNumber);
    List<Account> findByStatus(AccountStatus accountStatus);
    List<Account> findByCustomerId(Long customerId);
}
