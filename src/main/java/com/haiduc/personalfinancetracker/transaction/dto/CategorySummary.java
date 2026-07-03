package com.haiduc.personalfinancetracker.transaction.dto;

import com.haiduc.personalfinancetracker.common.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategorySummary {
    private UUID id;
    private String name;
    private TransactionType type;
}
