package com.haiduc.personalfinancetracker.budget.event;

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
public class BudgetAlertEvent {
    private UUID userId;
    private String userEmail;
    private String categoryName;
    private BigDecimal limitAmount;
    private BigDecimal spentAmount;
    private Double percentage;
    private AlertType alertType;
    private int month;
    private int year;

    public enum AlertType {
        ALERT_80, ALERT_100
    }
}
