package com.haiduc.personalfinancetracker.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseByCategory {
    private String categoryName;
    private BigDecimal amount;
    private Double percentage;
}