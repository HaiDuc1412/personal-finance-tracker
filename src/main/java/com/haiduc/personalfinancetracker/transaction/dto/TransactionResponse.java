package com.haiduc.personalfinancetracker.transaction.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
public class TransactionResponse {
    private UUID id;
    private BigDecimal amount;
    private String description;
    private LocalDate transactionDate;
    private CategorySummary category;
}
