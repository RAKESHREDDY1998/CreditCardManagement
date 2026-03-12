package com.creditcard.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    @JsonIgnore
    private CreditCard creditCard;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String merchantName;

    private String description;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @Column(nullable = false)
    private LocalDateTime transactionDate;

    private String referenceNumber;

    @Transient
    public String getCardNumber() {
        return creditCard != null ? creditCard.getCardNumber() : null;
    }

    public enum TransactionType {
        PURCHASE, REFUND, PAYMENT, CASH_ADVANCE, FEE
    }

    public enum TransactionStatus {
        PENDING, APPROVED, DECLINED, REVERSED
    }
}
