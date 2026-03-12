package com.creditcard.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CardRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Credit limit is required")
    @DecimalMin(value = "500.00", message = "Minimum credit limit is $500")
    @DecimalMax(value = "100000.00", message = "Maximum credit limit is $100,000")
    private BigDecimal creditLimit;
}
