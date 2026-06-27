package com.haiduc.personalfinancetracker.transaction.dto;

import com.haiduc.personalfinancetracker.common.enums.TransactionType;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class CategorySummary {
    private UUID id;
    private String name;
    private TransactionType type;
}
