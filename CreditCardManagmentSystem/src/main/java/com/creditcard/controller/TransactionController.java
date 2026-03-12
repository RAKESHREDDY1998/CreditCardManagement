package com.creditcard.controller;

import com.creditcard.dto.TransactionRequest;
import com.creditcard.model.Transaction;
import com.creditcard.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * POST /api/transactions/process
     * Processes a credit card transaction (purchase, payment, refund, etc.)
     */
    @PostMapping("/process")
    public ResponseEntity<Transaction> processTransaction(
            @Valid @RequestBody TransactionRequest request) {
        Transaction transaction = transactionService.processTransaction(request);
        HttpStatus status = transaction.getStatus() == Transaction.TransactionStatus.APPROVED
            ? HttpStatus.CREATED
            : HttpStatus.OK;
        return ResponseEntity.status(status).body(transaction);
    }

    /**
     * GET /api/transactions/card/{cardNumber}
     * Returns all transactions for a given card number (most recent first).
     */
    @GetMapping("/card/{cardNumber}")
    public ResponseEntity<List<Transaction>> getCardTransactions(
            @PathVariable String cardNumber) {
        return ResponseEntity.ok(transactionService.getCardTransactions(cardNumber));
    }

    /**
     * GET /api/transactions/card/{cardNumber}/range?startDate=...&endDate=...
     * Returns transactions within a date range for a card.
     */
    @GetMapping("/card/{cardNumber}/range")
    public ResponseEntity<List<Transaction>> getTransactionsByDateRange(
            @PathVariable String cardNumber,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                LocalDateTime endDate) {
        return ResponseEntity.ok(
            transactionService.getTransactionsByDateRange(cardNumber, startDate, endDate));
    }

    /**
     * GET /api/transactions/{id}
     * Returns a single transaction by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.getTransactionById(id));
    }
}
