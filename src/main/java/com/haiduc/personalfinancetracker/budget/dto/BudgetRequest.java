package com.haiduc.personalfinancetracker.budget.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class BudgetRequest {

    @NotNull(message = "Category ID is required")
    private UUID categoryId;

    @NotNull(message = "Limit amount is required")
    @DecimalMin(value = "0.01", message = "Limit amount must be greater than 0")
    @Digits(integer = 13, fraction = 2, message = "Invalid amount format")
    private BigDecimal limitAmount;

    @NotNull(message = "Month is required")
    @Min(value = 1, message = "Month must be between 1 and 12")
    @Max(value = 12, message = "Month must be between 1 and 12")
    private Short month;

    @NotNull(message = "Year is required")
    @Min(value = 2000, message = "Year must be 2000 or later")
    @Max(value = 2100, message = "Year must be 2100 or earlier")
    private Short year;
}
