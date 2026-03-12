package com.creditcard.service;

import com.creditcard.model.*;
import com.creditcard.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatementService {

    private final StatementRepository statementRepository;
    private final TransactionRepository transactionRepository;
    private final CreditCardRepository cardRepository;

    private static final BigDecimal MINIMUM_PAYMENT_RATE = new BigDecimal("0.05"); // 5%
    private static final BigDecimal MINIMUM_PAYMENT_FLOOR = new BigDecimal("25.00");
    private static final int BILLING_PERIOD_DAYS = 30;
    private static final int PAYMENT_DUE_DAYS = 21;

    /**
     * Generates a monthly statement for a specific credit card.
     */
    @Transactional
    public Statement generateStatement(CreditCard card) {
        LocalDate statementDate = LocalDate.now();
        LocalDate dueDate = statementDate.plusDays(PAYMENT_DUE_DAYS);

        // Fetch transactions for the billing period
        LocalDateTime periodStart = LocalDateTime.now().minusDays(BILLING_PERIOD_DAYS);
        LocalDateTime periodEnd = LocalDateTime.now();

        List<Transaction> periodTransactions = transactionRepository
            .findByCardAndDateRange(card, periodStart, periodEnd);

        // Calculate total charges (purchases + fees + cash advances)
        BigDecimal totalCharges = periodTransactions.stream()
            .filter(t -> t.getStatus() == Transaction.TransactionStatus.APPROVED)
            .filter(t -> t.getType() == Transaction.TransactionType.PURCHASE
                || t.getType() == Transaction.TransactionType.CASH_ADVANCE
                || t.getType() == Transaction.TransactionType.FEE)
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate total payments/refunds
        BigDecimal totalPayments = periodTransactions.stream()
            .filter(t -> t.getStatus() == Transaction.TransactionStatus.APPROVED)
            .filter(t -> t.getType() == Transaction.TransactionType.PAYMENT
                || t.getType() == Transaction.TransactionType.REFUND)
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal closingBalance = card.getCurrentBalance();
        BigDecimal openingBalance = closingBalance.subtract(totalCharges).add(totalPayments);
        BigDecimal totalAmountDue = closingBalance.max(BigDecimal.ZERO);

        // Calculate minimum payment
        BigDecimal minimumPayment = BigDecimal.ZERO;
        if (totalAmountDue.compareTo(BigDecimal.ZERO) > 0) {
            minimumPayment = totalAmountDue.multiply(MINIMUM_PAYMENT_RATE)
                .setScale(2, RoundingMode.HALF_UP);
            if (minimumPayment.compareTo(MINIMUM_PAYMENT_FLOOR) < 0) {
                minimumPayment = totalAmountDue.min(MINIMUM_PAYMENT_FLOOR);
            }
        }

        Statement statement = Statement.builder()
            .creditCard(card)
            .statementDate(statementDate)
            .dueDate(dueDate)
            .totalAmount(totalAmountDue)
            .minimumPayment(minimumPayment)
            .openingBalance(openingBalance.setScale(2, RoundingMode.HALF_UP))
            .closingBalance(closingBalance.setScale(2, RoundingMode.HALF_UP))
            .status(Statement.StatementStatus.GENERATED)
            .generatedAt(LocalDateTime.now())
            .transactions(periodTransactions)
            .build();

        Statement saved = statementRepository.save(statement);
        log.info("Statement generated | Card: *{} | Period: {} | Total Due: ${}",
            card.getCardNumber().substring(card.getCardNumber().length() - 4),
            statementDate,
            totalAmountDue);
        return saved;
    }

    /**
     * Triggered by Quartz Scheduler — generates statements for ALL active cards.
     */
    @Transactional
    public void generateStatementsForAllActiveCards() {
        List<CreditCard> activeCards = cardRepository.findByStatus(CreditCard.CardStatus.ACTIVE);
        log.info("Quartz Job: Generating statements for {} active cards...", activeCards.size());

        int success = 0, failed = 0;
        for (CreditCard card : activeCards) {
            try {
                generateStatement(card);
                success++;
            } catch (Exception e) {
                failed++;
                log.error("Failed to generate statement for card *{}: {}",
                    card.getCardNumber().substring(card.getCardNumber().length() - 4),
                    e.getMessage());
            }
        }
        log.info("Statement generation complete. Success: {}, Failed: {}", success, failed);
    }

    public List<Statement> getCardStatements(Long cardId) {
        CreditCard card = cardRepository.findById(cardId)
            .orElseThrow(() -> new RuntimeException("Card not found with ID: " + cardId));
        return statementRepository.findByCreditCardOrderByStatementDateDesc(card);
    }

    public Statement getStatementById(Long statementId) {
        return statementRepository.findById(statementId)
            .orElseThrow(() -> new RuntimeException("Statement not found with ID: " + statementId));
    }
}
