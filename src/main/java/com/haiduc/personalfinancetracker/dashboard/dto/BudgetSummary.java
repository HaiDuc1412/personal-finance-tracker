package com.haiduc.personalfinancetracker.dashboard.dto;

import com.haiduc.personalfinancetracker.transaction.dto.CategorySummary;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class BudgetSummary {
    private UUID budgetId;
    private CategorySummary category;
    private BigDecimal limitAmount;
    private BigDecimal spentAmount;
    private BigDecimal remainingAmount;
    private Double percentage;
}
