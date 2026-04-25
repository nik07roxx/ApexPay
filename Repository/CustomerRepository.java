package com.nik07roxx.apexPay.Repository;

import com.nik07roxx.apexPay.Entity.Customer;
import com.nik07roxx.apexPay.model.CustomerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer,Long> {
    List<Customer> findByStatus(CustomerStatus customerStatus);
    boolean existsByEmail(String email);
}
