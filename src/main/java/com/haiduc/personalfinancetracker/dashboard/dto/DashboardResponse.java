package com.haiduc.personalfinancetracker.dashboard.dto;

import com.haiduc.personalfinancetracker.transaction.dto.TransactionResponse;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DashboardResponse {
    private Integer month;
    private Integer year;
    private MonthlySummary monthlySummary;
    private List<BudgetSummary> budgetSummaries;
    private List<TransactionResponse> recentTransactions;
    private List<ExpenseByCategory> expenseByCategory;
}