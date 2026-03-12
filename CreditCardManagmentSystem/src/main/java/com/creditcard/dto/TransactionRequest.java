package com.creditcard.dto;

import com.creditcard.model.Transaction;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransactionRequest {

    @NotBlank(message = "Card number is required")
    private String cardNumber;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "Merchant name is required")
    private String merchantName;

    private String description;

    @NotNull(message = "Transaction type is required")
    private Transaction.TransactionType type;
}
