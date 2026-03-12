package com.creditcard.service;

import com.creditcard.dto.TransactionRequest;
import com.creditcard.model.CreditCard;
import com.creditcard.model.Transaction;
import com.creditcard.repository.CreditCardRepository;
import com.creditcard.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CreditCardRepository cardRepository;

    @Mock
    private CardService cardService;

    @InjectMocks
    private TransactionService transactionService;

    private CreditCard activeCard;

    @BeforeEach
    void setUp() {
        activeCard = CreditCard.builder()
            .id(1L)
            .cardNumber("4111 1111 1111 1111")
            .creditLimit(new BigDecimal("5000.00"))
            .availableBalance(new BigDecimal("4000.00"))
            .currentBalance(new BigDecimal("1000.00"))
            .status(CreditCard.CardStatus.ACTIVE)
            .expiryDate(LocalDate.now().plusYears(2))
            .issuedAt(LocalDateTime.now())
            .build();
    }

    @Test
    void processTransaction_purchase_approved() {
        TransactionRequest request = new TransactionRequest();
        request.setCardNumber("4111 1111 1111 1111");
        request.setAmount(new BigDecimal("100.00"));
        request.setMerchantName("Test Store");
        request.setDescription("Test purchase");
        request.setType(Transaction.TransactionType.PURCHASE);

        when(cardRepository.findByCardNumber("4111 1111 1111 1111"))
            .thenReturn(Optional.of(activeCard));
        when(transactionRepository.save(any(Transaction.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = transactionService.processTransaction(request);

        assertEquals(Transaction.TransactionStatus.APPROVED, result.getStatus());
        assertEquals(new BigDecimal("100.00"), result.getAmount());
        verify(cardService).updateCardBalance(activeCard, new BigDecimal("100.00"), true);
    }

    @Test
    void processTransaction_purchase_declined_insufficientBalance() {
        TransactionRequest request = new TransactionRequest();
        request.setCardNumber("4111 1111 1111 1111");
        request.setAmount(new BigDecimal("5000.00"));
        request.setMerchantName("Expensive Store");
        request.setDescription("Big purchase");
        request.setType(Transaction.TransactionType.PURCHASE);

        when(cardRepository.findByCardNumber("4111 1111 1111 1111"))
            .thenReturn(Optional.of(activeCard));
        when(transactionRepository.save(any(Transaction.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = transactionService.processTransaction(request);

        assertEquals(Transaction.TransactionStatus.DECLINED, result.getStatus());
        verify(cardService, never()).updateCardBalance(any(), any(), anyBoolean());
    }

    @Test
    void processTransaction_payment_cappedToCurrentBalance() {
        TransactionRequest request = new TransactionRequest();
        request.setCardNumber("4111 1111 1111 1111");
        request.setAmount(new BigDecimal("2000.00"));
        request.setMerchantName("Payment");
        request.setDescription("Overpayment test");
        request.setType(Transaction.TransactionType.PAYMENT);

        when(cardRepository.findByCardNumber("4111 1111 1111 1111"))
            .thenReturn(Optional.of(activeCard));
        when(transactionRepository.save(any(Transaction.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = transactionService.processTransaction(request);

        assertEquals(Transaction.TransactionStatus.APPROVED, result.getStatus());
        // Payment should be capped to currentBalance (1000.00)
        assertEquals(new BigDecimal("1000.00"), result.getAmount());
        verify(cardService).updateCardBalance(activeCard, new BigDecimal("1000.00"), false);
    }

    @Test
    void processTransaction_payment_withinBalance() {
        TransactionRequest request = new TransactionRequest();
        request.setCardNumber("4111 1111 1111 1111");
        request.setAmount(new BigDecimal("500.00"));
        request.setMerchantName("Payment");
        request.setDescription("Normal payment");
        request.setType(Transaction.TransactionType.PAYMENT);

        when(cardRepository.findByCardNumber("4111 1111 1111 1111"))
            .thenReturn(Optional.of(activeCard));
        when(transactionRepository.save(any(Transaction.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = transactionService.processTransaction(request);

        assertEquals(Transaction.TransactionStatus.APPROVED, result.getStatus());
        assertEquals(new BigDecimal("500.00"), result.getAmount());
        verify(cardService).updateCardBalance(activeCard, new BigDecimal("500.00"), false);
    }

    @Test
    void processTransaction_blockedCard_throwsException() {
        activeCard.setStatus(CreditCard.CardStatus.BLOCKED);

        TransactionRequest request = new TransactionRequest();
        request.setCardNumber("4111 1111 1111 1111");
        request.setAmount(new BigDecimal("100.00"));
        request.setMerchantName("Test Store");
        request.setType(Transaction.TransactionType.PURCHASE);

        when(cardRepository.findByCardNumber("4111 1111 1111 1111"))
            .thenReturn(Optional.of(activeCard));

        assertThrows(RuntimeException.class, () ->
            transactionService.processTransaction(request));
    }

    @Test
    void processTransaction_expiredCard_throwsException() {
        activeCard.setExpiryDate(LocalDate.now().minusDays(1));
        activeCard.setStatus(CreditCard.CardStatus.ACTIVE);

        TransactionRequest request = new TransactionRequest();
        request.setCardNumber("4111 1111 1111 1111");
        request.setAmount(new BigDecimal("100.00"));
        request.setMerchantName("Test Store");
        request.setType(Transaction.TransactionType.PURCHASE);

        when(cardRepository.findByCardNumber("4111 1111 1111 1111"))
            .thenReturn(Optional.of(activeCard));

        assertThrows(RuntimeException.class, () ->
            transactionService.processTransaction(request));
    }
}
