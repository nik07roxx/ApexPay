package com.nik07roxx.apexPay.Entity;

import com.nik07roxx.apexPay.model.AccountStatus;
import com.nik07roxx.apexPay.model.AccountType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "account")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "account_number", unique = true)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type")
    private AccountType accountType;

    @Column(name = "balance")
    private BigDecimal balance;

    @Column(name = "interest_rate")
    private BigDecimal interestRate;

    @CreationTimestamp
    @Column(name = "opening_date")
    private LocalDateTime openingDate;

    @Column(name = "status")
    private AccountStatus status;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.EAGER,
            cascade = CascadeType.REFRESH)
    @JoinColumn(name = "customer_id")
    private Customer customer;
}
