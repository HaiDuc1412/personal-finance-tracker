package com.haiduc.personalfinancetracker.category.dto;

import com.haiduc.personalfinancetracker.common.enums.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class CategoryRequest {
    @NotBlank(message = "Category name is required")
    @Size(max = 50, message = "Category name must not exceed 50 characters")
    private String name;

    @NotNull(message = "Transaction type is required")
    private TransactionType type;
}
