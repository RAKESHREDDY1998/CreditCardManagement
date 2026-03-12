package com.creditcard.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "statements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Statement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    @JsonIgnore
    private CreditCard creditCard;

    @Column(nullable = false)
    private LocalDate statementDate;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private BigDecimal minimumPayment;

    private BigDecimal openingBalance;
    private BigDecimal closingBalance;

    @Enumerated(EnumType.STRING)
    private StatementStatus status;

    @Column(nullable = false)
    private LocalDateTime generatedAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "statement_transactions",
        joinColumns = @JoinColumn(name = "statement_id"),
        inverseJoinColumns = @JoinColumn(name = "transaction_id")
    )
    private List<Transaction> transactions;

    @Transient
    public String getCardNumber() {
        return creditCard != null ? creditCard.getCardNumber() : null;
    }

    @Transient
    public Long getCardId() {
        return creditCard != null ? creditCard.getId() : null;
    }

    public enum StatementStatus {
        GENERATED, SENT, PAID, OVERDUE
    }
}
