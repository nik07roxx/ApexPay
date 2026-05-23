package com.nik07roxx.apexPay.Entity;

import com.nik07roxx.apexPay.model.CurrencyType;
import com.nik07roxx.apexPay.model.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class Transactions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

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

    @Column(name = "source_amount", nullable = false)
    private BigDecimal sourceAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_currency", nullable = false)
    private CurrencyType sourceCurrency;

    // Only used when transaction type is TRANSFER, for the account being credited
    @Column(name = "target_account")
    private String targetAccount;

    @Column(name = "target_amount", nullable = false)
    private BigDecimal targetAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_currency", nullable = false)
    private CurrencyType targetCurrency;

    @Column(name = "exchange_rate", nullable = false)
    private BigDecimal exchangeRate;

    @Override
    public boolean equals(Object o)
    {
        if(this == o) return true;
        if (!(o instanceof Transactions)) return false;
        Transactions transactions = (Transactions) o;
        return id != null && id.equals(transactions.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
