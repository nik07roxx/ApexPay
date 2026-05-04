package com.nik07roxx.apexPay.Repository;

import com.nik07roxx.apexPay.Entity.Customer;
import com.nik07roxx.apexPay.model.CustomerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer,Long> {
    Page<Customer> findByStatus(CustomerStatus customerStatus, Pageable pageable);
    boolean existsByEmail(String email);
}
