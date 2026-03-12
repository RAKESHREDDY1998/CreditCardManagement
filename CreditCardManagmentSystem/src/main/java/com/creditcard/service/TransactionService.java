package com.creditcard.service;

import com.creditcard.dto.TransactionRequest;
import com.creditcard.model.*;
import com.creditcard.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CreditCardRepository cardRepository;
    private final CardService cardService;

    @Transactional
    public Transaction processTransaction(TransactionRequest request) {
        CreditCard card = cardRepository.findByCardNumber(request.getCardNumber())
            .orElseThrow(() -> new RuntimeException("Card not found: " + request.getCardNumber()));

        // --- Pre-authorization checks ---
        validateCard(card);

        Transaction.TransactionStatus status;
        String logMessage;
        BigDecimal processedAmount = request.getAmount();

        switch (request.getType()) {
            case PURCHASE, CASH_ADVANCE -> {
                if (card.getAvailableBalance().compareTo(request.getAmount()) < 0) {
                    status = Transaction.TransactionStatus.DECLINED;
                    logMessage = "DECLINED - Insufficient balance";
                } else {
                    cardService.updateCardBalance(card, request.getAmount(), true);
                    status = Transaction.TransactionStatus.APPROVED;
                    logMessage = "APPROVED";
                }
            }
            case PAYMENT, REFUND -> {
                // Ensure payment does not exceed current balance
                BigDecimal maxPayment = card.getCurrentBalance();
                if (processedAmount.compareTo(maxPayment) > 0 && request.getType() == Transaction.TransactionType.PAYMENT) {
                    processedAmount = maxPayment;
                }
                cardService.updateCardBalance(card, processedAmount, false);
                status = Transaction.TransactionStatus.APPROVED;
                logMessage = "APPROVED - Payment/Refund processed";
            }
            case FEE -> {
                cardService.updateCardBalance(card, request.getAmount(), true);
                status = Transaction.TransactionStatus.APPROVED;
                logMessage = "APPROVED - Fee applied";
            }
            default -> {
                status = Transaction.TransactionStatus.DECLINED;
                logMessage = "DECLINED - Unknown type";
            }
        }

        Transaction transaction = Transaction.builder()
            .creditCard(card)
            .amount(processedAmount)
            .merchantName(request.getMerchantName())
            .description(request.getDescription())
            .type(request.getType())
            .status(status)
            .transactionDate(LocalDateTime.now())
            .referenceNumber(generateReferenceNumber())
            .build();

        Transaction saved = transactionRepository.save(transaction);
        log.info("Transaction [{}] {} | Card: *{} | Amount: ${} | Merchant: {} | {}",
            saved.getReferenceNumber(),
            saved.getType(),
            card.getCardNumber().substring(card.getCardNumber().length() - 4),
            processedAmount,
            request.getMerchantName(),
            logMessage);
        return saved;
    }

    public List<Transaction> getCardTransactions(String cardNumber) {
        CreditCard card = cardRepository.findByCardNumber(cardNumber)
            .orElseThrow(() -> new RuntimeException("Card not found: " + cardNumber));
        return transactionRepository.findByCreditCardOrderByTransactionDateDesc(card);
    }

    public List<Transaction> getTransactionsByDateRange(
            String cardNumber,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        CreditCard card = cardRepository.findByCardNumber(cardNumber)
            .orElseThrow(() -> new RuntimeException("Card not found: " + cardNumber));
        return transactionRepository.findByCardAndDateRange(card, startDate, endDate);
    }

    public Transaction getTransactionById(Long id) {
        return transactionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Transaction not found with ID: " + id));
    }

    // ===================== Helper Methods =====================

    private void validateCard(CreditCard card) {
        if (card.getStatus() == CreditCard.CardStatus.BLOCKED) {
            throw new RuntimeException("Card is blocked. Please contact customer support.");
        }
        if (card.getStatus() == CreditCard.CardStatus.EXPIRED) {
            throw new RuntimeException("Card has expired.");
        }
        if (card.getExpiryDate().isBefore(LocalDate.now())) {
            card.setStatus(CreditCard.CardStatus.EXPIRED);
            cardRepository.save(card);
            throw new RuntimeException("Card has expired.");
        }
        if (card.getStatus() == CreditCard.CardStatus.PENDING) {
            throw new RuntimeException("Card is not yet activated.");
        }
    }

    private String generateReferenceNumber() {
        return "TXN" + UUID.randomUUID().toString().replace("-", "").substring(0, 9).toUpperCase();
    }
}
