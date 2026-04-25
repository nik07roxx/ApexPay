package com.nik07roxx.apexPay.Entity;

import com.nik07roxx.apexPay.model.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Transactions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Column(name = "transaction_reference", unique = true, nullable = false, length = 36)
    private String transactionReference;

    @Column(name = "description")
    private String description;

    @CreationTimestamp
    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    // The account the transaction is happening to
    @Column(name = "source_account")
    private String sourceAccount;

    // Only used when transaction type is TRANSFER, for the account being credited
    @Column(name = "target_account")
    private String targetAccount;
}
