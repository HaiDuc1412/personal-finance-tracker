package com.haiduc.personalfinancetracker.budget.dto;

import com.haiduc.personalfinancetracker.transaction.dto.CategorySummary;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetResponse {

    private UUID id;
    private CategorySummary category;
    private BigDecimal limitAmount;
    private BigDecimal spentAmount;
    private BigDecimal remainingAmount;
    private Double percentage;
    private Short month;
    private Short year;
    private boolean alertSent80;
    private boolean alertSent100;
}
