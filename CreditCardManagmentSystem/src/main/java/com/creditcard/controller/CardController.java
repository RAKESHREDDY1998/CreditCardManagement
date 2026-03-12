package com.creditcard.controller;

import com.creditcard.dto.CardRequest;
import com.creditcard.model.CreditCard;
import com.creditcard.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    /**
     * POST /api/cards/issue  [ADMIN ONLY]
     * Simulates card issuance — creates a new credit card for a user.
     */
    @PostMapping("/issue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CreditCard> issueCard(
            @Valid @RequestBody CardRequest cardRequest) {
        CreditCard card = cardService.issueCard(cardRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(card);
    }

    /**
     * GET /api/cards/user/{userId}  [ADMIN or card owner]
     * Returns all cards assigned to a specific user.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CreditCard>> getCardsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(cardService.getCardsByUser(userId));
    }

    /**
     * GET /api/cards/{cardId}
     * Returns details of a specific card by ID.
     */
    @GetMapping("/{cardId}")
    public ResponseEntity<CreditCard> getCard(@PathVariable Long cardId) {
        return ResponseEntity.ok(cardService.getCardById(cardId));
    }

    /**
     * PUT /api/cards/{cardId}/block  [ADMIN ONLY]
     * Blocks an active credit card.
     */
    @PutMapping("/{cardId}/block")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CreditCard> blockCard(@PathVariable Long cardId) {
        return ResponseEntity.ok(cardService.blockCard(cardId));
    }

    /**
     * PUT /api/cards/{cardId}/activate  [ADMIN ONLY]
     * Re-activates a blocked credit card.
     */
    @PutMapping("/{cardId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CreditCard> activateCard(@PathVariable Long cardId) {
        return ResponseEntity.ok(cardService.activateCard(cardId));
    }

    /**
     * GET /api/cards/all  [ADMIN ONLY]
     * Returns all active credit cards (for admin dashboard).
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CreditCard>> getAllActiveCards() {
        return ResponseEntity.ok(cardService.getAllActiveCards());
    }

    /**
     * GET /api/cards/number/{cardNumber}
     * Returns card details by card number.
     */
    @GetMapping("/number/{cardNumber}")
    public ResponseEntity<CreditCard> getCardByNumber(@PathVariable String cardNumber) {
        return ResponseEntity.ok(cardService.getCardByNumber(cardNumber));
    }
}
