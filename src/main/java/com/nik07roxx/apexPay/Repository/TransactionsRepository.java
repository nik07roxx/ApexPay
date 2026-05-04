package com.nik07roxx.apexPay.Repository;

import com.nik07roxx.apexPay.Entity.Transactions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionsRepository extends JpaRepository<Transactions,Long> {
    Page<Transactions> findByTransactionReferenceStartingWith(String transactionReference, Pageable pageable);
    Page<Transactions> findBySourceAccountOrTargetAccountOrderByTimestampDesc(String sourceAccount,
                                                                              String targetAccount,
                                                                              Pageable pageable);
}
