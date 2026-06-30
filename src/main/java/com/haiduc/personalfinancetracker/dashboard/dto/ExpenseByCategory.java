package com.haiduc.personalfinancetracker.dashboard.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ExpenseByCategory {
    private String categoryName;
    private BigDecimal amount;
    private Double percentage;
}