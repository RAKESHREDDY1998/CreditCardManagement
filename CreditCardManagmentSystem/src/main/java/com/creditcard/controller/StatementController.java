package com.creditcard.controller;

import com.creditcard.model.*;
import com.creditcard.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statements")
@RequiredArgsConstructor
public class StatementController {

    private final StatementService statementService;
    private final CardService cardService;

    /**
     * GET /api/statements/card/{cardId}
     * Returns all statements for a card (most recent first).
     */
    @GetMapping("/card/{cardId}")
    public ResponseEntity<List<Statement>> getCardStatements(@PathVariable Long cardId) {
        return ResponseEntity.ok(statementService.getCardStatements(cardId));
    }

    /**
     * GET /api/statements/{statementId}
     * Returns a specific statement with transaction details.
     */
    @GetMapping("/{statementId}")
    public ResponseEntity<Statement> getStatement(@PathVariable Long statementId) {
        return ResponseEntity.ok(statementService.getStatementById(statementId));
    }

    /**
     * POST /api/statements/generate/{cardId}  [ADMIN ONLY]
     * Manually generates a statement for a specific card.
     */
    @PostMapping("/generate/{cardId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Statement> generateStatement(@PathVariable Long cardId) {
        CreditCard card = cardService.getCardById(cardId);
        Statement statement = statementService.generateStatement(card);
        return ResponseEntity.status(HttpStatus.CREATED).body(statement);
    }

    /**
     * POST /api/statements/generate-all  [ADMIN ONLY]
     * Triggers bulk statement generation for all active cards.
     * (Same as Quartz job — can be triggered manually for testing)
     */
    @PostMapping("/generate-all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> generateAllStatements() {
        statementService.generateStatementsForAllActiveCards();
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "Monthly statement generation triggered for all active cards"
        ));
    }
}
