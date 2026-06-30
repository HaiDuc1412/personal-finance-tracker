package com.haiduc.personalfinancetracker.dashboard;

import com.haiduc.personalfinancetracker.budget.Budget;
import com.haiduc.personalfinancetracker.budget.BudgetRepository;
import com.haiduc.personalfinancetracker.common.enums.TransactionType;
import com.haiduc.personalfinancetracker.dashboard.dto.BudgetSummary;
import com.haiduc.personalfinancetracker.dashboard.dto.DashboardResponse;
import com.haiduc.personalfinancetracker.dashboard.dto.ExpenseByCategory;
import com.haiduc.personalfinancetracker.dashboard.dto.MonthlySummary;
import com.haiduc.personalfinancetracker.transaction.Transaction;
import com.haiduc.personalfinancetracker.transaction.TransactionRepository;
import com.haiduc.personalfinancetracker.transaction.dto.CategorySummary;
import com.haiduc.personalfinancetracker.transaction.dto.TransactionResponse;
import com.haiduc.personalfinancetracker.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;

    @Cacheable(value = "dashboard", key = "#currentUser.id + ':' + #resolvedMonth + ':' + #resolvedYear")
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(User currentUser, Integer month, Integer year) {
        // Default tháng hiện tại
        LocalDate now = LocalDate.now();
        int resolvedMonth = (month != null) ? month : now.getMonthValue();
        int resolvedYear = (year != null) ? year : now.getYear();

        MonthlySummary monthlySummary = buildMonthlySummary(currentUser, resolvedMonth, resolvedYear);
        List<BudgetSummary> budgets = buildBudgetSummaries(currentUser, resolvedMonth, resolvedYear);
        List<TransactionResponse> recent = buildRecentTransactions(currentUser, resolvedMonth, resolvedYear);
        List<ExpenseByCategory> expense = buildExpenseByCategory(currentUser, resolvedMonth, resolvedYear);

        return DashboardResponse.builder()
                .month(resolvedMonth)
                .year(resolvedYear)
                .monthlySummary(monthlySummary)
                .budgetSummaries(budgets)
                .recentTransactions(recent)
                .expenseByCategory(expense)
                .build();
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private MonthlySummary buildMonthlySummary(User user, int month, int year) {
        BigDecimal totalIncome = transactionRepository.sumByUserAndTypeAndMonthAndYear(
                user, TransactionType.INCOME, month, year);
        BigDecimal totalExpense = transactionRepository.sumByUserAndTypeAndMonthAndYear(
                user, TransactionType.EXPENSE, month, year);
        BigDecimal net = totalIncome.subtract(totalExpense);

        return MonthlySummary.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .netAmount(net)
                .build();
    }

    private List<BudgetSummary> buildBudgetSummaries(User user, int month, int year) {
        List<Budget> budgets = budgetRepository.findAllByUserAndMonthAndYear(
                user, (short) month, (short) year);

        return budgets.stream().map(budget -> {
            BigDecimal spent = budgetRepository.sumSpentByUserAndCategoryAndMonthAndYear(
                    user, budget.getCategory(), (short) month, (short) year);
            BigDecimal limit = budget.getMonthlyLimit();
            BigDecimal remaining = limit.subtract(spent);
            double percentage = limit.compareTo(BigDecimal.ZERO) == 0 ? 0.0
                    : spent.divide(limit, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();

            return BudgetSummary.builder()
                    .budgetId(budget.getId())
                    .category(CategorySummary.builder()
                            .id(budget.getCategory().getId())
                            .name(budget.getCategory().getName())
                            .type(budget.getCategory().getType())
                            .build())
                    .limitAmount(limit)
                    .spentAmount(spent)
                    .remainingAmount(remaining)
                    .percentage(Math.round(percentage * 100.0) / 100.0)
                    .build();
        }).toList();
    }

    private List<TransactionResponse> buildRecentTransactions(User user, int month, int year) {
        return transactionRepository
                .findTop5ByUserAndMonthAndYear(user, month, year, PageRequest.of(0, 5))
                .stream()
                .map(this::toTransactionResponse)
                .toList();
    }

    private List<ExpenseByCategory> buildExpenseByCategory(User user, int month, int year) {
        List<Object[]> rows = transactionRepository
                .findExpenseGroupedByCategoryAndMonth(user, month, year);

        // Tính tổng để tính %
        BigDecimal total = rows.stream()
                .map(row -> (BigDecimal) row[1])
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return rows.stream().map(row -> {
            String categoryName = (String) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            double percentage = total.compareTo(BigDecimal.ZERO) == 0 ? 0.0
                    : amount.divide(total, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();

            return ExpenseByCategory.builder()
                    .categoryName(categoryName)
                    .amount(amount)
                    .percentage(Math.round(percentage * 100.0) / 100.0)
                    .build();
        }).toList();
    }

    private TransactionResponse toTransactionResponse(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .amount(t.getAmount())
                .description(t.getDescription())
                .transactionDate(t.getTransactionDate())
                .category(CategorySummary.builder()
                        .id(t.getCategory().getId())
                        .name(t.getCategory().getName())
                        .type(t.getCategory().getType())
                        .build())
                .build();
    }

}

