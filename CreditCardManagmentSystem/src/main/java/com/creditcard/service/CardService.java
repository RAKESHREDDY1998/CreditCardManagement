package com.creditcard.service;

import com.creditcard.dto.CardRequest;
import com.creditcard.model.*;
import com.creditcard.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardService {

    private final CreditCardRepository cardRepository;
    private final UserRepository userRepository;

    /**
     * Issues a new credit card to a user. Only ADMIN can call this.
     */
    @Transactional
    public CreditCard issueCard(CardRequest request) {
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new RuntimeException("User not found with ID: " + request.getUserId()));

        String cardNumber = generateCardNumber();
        String cvv = generateCVV();

        CreditCard card = CreditCard.builder()
            .cardNumber(cardNumber)
            .user(user)
            .creditLimit(request.getCreditLimit())
            .availableBalance(request.getCreditLimit())
            .currentBalance(BigDecimal.ZERO)
            .status(CreditCard.CardStatus.ACTIVE)
            .expiryDate(LocalDate.now().plusYears(5))
            .cvv(cvv)
            .issuedAt(LocalDateTime.now())
            .build();

        CreditCard savedCard = cardRepository.save(card);
        log.info("New credit card issued: {} for user: {} | Limit: ${}",
            maskCardNumber(cardNumber), user.getUsername(), request.getCreditLimit());
        return savedCard;
    }

    public List<CreditCard> getCardsByUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        return cardRepository.findByUser(user);
    }

    public CreditCard getCardById(Long cardId) {
        return cardRepository.findById(cardId)
            .orElseThrow(() -> new RuntimeException("Card not found with ID: " + cardId));
    }

    public CreditCard getCardByNumber(String cardNumber) {
        return cardRepository.findByCardNumber(cardNumber)
            .orElseThrow(() -> new RuntimeException("Card not found: " + cardNumber));
    }

    @Transactional
    public CreditCard blockCard(Long cardId) {
        CreditCard card = getCardById(cardId);
        if (card.getStatus() == CreditCard.CardStatus.BLOCKED) {
            throw new RuntimeException("Card is already blocked");
        }
        card.setStatus(CreditCard.CardStatus.BLOCKED);
        CreditCard updated = cardRepository.save(card);
        log.info("Card BLOCKED: {}", maskCardNumber(card.getCardNumber()));
        return updated;
    }

    @Transactional
    public CreditCard activateCard(Long cardId) {
        CreditCard card = getCardById(cardId);
        if (card.getStatus() == CreditCard.CardStatus.ACTIVE) {
            throw new RuntimeException("Card is already active");
        }
        card.setStatus(CreditCard.CardStatus.ACTIVE);
        CreditCard updated = cardRepository.save(card);
        log.info("Card ACTIVATED: {}", maskCardNumber(card.getCardNumber()));
        return updated;
    }

    @Transactional
    public void updateCardBalance(CreditCard card, BigDecimal amount, boolean isDebit) {
        if (isDebit) {
            // Debit: increase used balance, decrease available balance
            card.setCurrentBalance(card.getCurrentBalance().add(amount));
            card.setAvailableBalance(card.getAvailableBalance().subtract(amount));
        } else {
            // Credit/Payment: decrease used balance, increase available balance
            card.setCurrentBalance(card.getCurrentBalance().subtract(amount));
            card.setAvailableBalance(card.getAvailableBalance().add(amount));
        }
        cardRepository.save(card);
    }

    public List<CreditCard> getAllActiveCards() {
        return cardRepository.findByStatus(CreditCard.CardStatus.ACTIVE);
    }

    // ===================== Helper Methods =====================

    private String generateCardNumber() {
        String cardNumber;
        Random random = new Random();
        do {
            // Generate 16-digit Visa-style card number
            StringBuilder sb = new StringBuilder("4"); // Visa prefix
            for (int i = 0; i < 15; i++) {
                sb.append(random.nextInt(10));
            }
            // Format as XXXX XXXX XXXX XXXX
            String raw = sb.toString();
            cardNumber = raw.substring(0, 4) + " " + raw.substring(4, 8) + " "
                + raw.substring(8, 12) + " " + raw.substring(12, 16);
        } while (cardRepository.existsByCardNumber(cardNumber));

        return cardNumber;
    }

    private String generateCVV() {
        return String.format("%03d", new Random().nextInt(1000));
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) return "****";
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }
}
