package com.haiduc.personalfinancetracker.budget;

import com.haiduc.personalfinancetracker.budget.dto.BudgetRequest;
import com.haiduc.personalfinancetracker.budget.dto.BudgetResponse;
import com.haiduc.personalfinancetracker.category.Category;
import com.haiduc.personalfinancetracker.category.CategoryRepository;
import com.haiduc.personalfinancetracker.common.enums.TransactionType;
import com.haiduc.personalfinancetracker.common.exception.AppException;
import com.haiduc.personalfinancetracker.common.exception.DuplicateResourceException;
import com.haiduc.personalfinancetracker.common.exception.ResourceNotFoundException;
import com.haiduc.personalfinancetracker.transaction.dto.CategorySummary;
import com.haiduc.personalfinancetracker.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;


    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "budgets",    allEntries = true),
            @CacheEvict(value = "dashboard",  allEntries = true)
    })
    public BudgetResponse createBudget(BudgetRequest request) {
        User currentUser = getCurrentUser();

        Category category = categoryRepository
                .findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        if (category.getType() == TransactionType.INCOME) {
            throw new AppException("Cannot set budget for INCOME category", HttpStatus.BAD_REQUEST);
        }

        if (category.getUser() != null && !category.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Category", "id", request.getCategoryId());
        }

        if (budgetRepository.existsByUserAndCategoryAndMonthAndYear(
                currentUser, category, request.getMonth(), request.getYear())) {
            throw new DuplicateResourceException(
                    "Budget already exists for this category in " +
                            request.getMonth() + "/" + request.getYear());
        }

        Budget budget = Budget.builder()
                .user(currentUser)
                .category(category)
                .monthlyLimit(request.getLimitAmount())
                .month(request.getMonth())
                .year(request.getYear())
                .alertSent80(false)
                .alertSent100(false)
                .build();

        return toResponse(budgetRepository.save(budget));
    }


    @Cacheable(value = "budgets", key = "#currentUser.id + ':' + #resolvedMonth + ':' + #resolvedYear")
    @Transactional(readOnly = true)
    public List<BudgetResponse> getBudgets(User currentUser, Short month, Short year) {
        LocalDate now = LocalDate.now();
        Short resolvedMonth = (month != null) ? month : (short) now.getMonthValue();
        Short resolvedYear = (year != null) ? year : (short) now.getYear();

        return budgetRepository
                .findAllByUserAndMonthAndYear(currentUser, resolvedMonth, resolvedYear)
                .stream()
                .map(this::toResponse)
                .toList();
    }


    @Transactional(readOnly = true)
    public BudgetResponse getBudget(UUID id) {
        User currentUser = getCurrentUser();
        Budget budget = findBudgetOrThrow(id, currentUser);
        return toResponse(budget);
    }


    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "budgets",   allEntries = true),
            @CacheEvict(value = "dashboard", allEntries = true)
    })
    public BudgetResponse updateBudget(UUID id, BudgetRequest request) {
        User currentUser = getCurrentUser();
        Budget budget = findBudgetOrThrow(id, currentUser);

        Category category = categoryRepository
                .findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        if (category.getType() == TransactionType.INCOME) {
            throw new AppException("Cannot set budget for INCOME category", HttpStatus.BAD_REQUEST);
        }

        if (category.getUser() != null && !category.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Category", "id", request.getCategoryId());
        }

        if (budgetRepository.existsByUserAndCategoryAndMonthAndYearAndIdNot(
                currentUser, category, request.getMonth(), request.getYear(), id)) {
            throw new DuplicateResourceException(
                    "Budget already exists for this category in " +
                            request.getMonth() + "/" + request.getYear());
        }

        budget.setCategory(category);
        budget.setMonthlyLimit(request.getLimitAmount());
        budget.setMonth(request.getMonth());
        budget.setYear(request.getYear());
        budget.setAlertSent80(false);
        budget.setAlertSent100(false);

        return toResponse(budgetRepository.save(budget));
    }


    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "budgets",   allEntries = true),
            @CacheEvict(value = "dashboard", allEntries = true)
    })
    public void deleteBudget(UUID id) {
        User currentUser = getCurrentUser();
        Budget budget = findBudgetOrThrow(id, currentUser);
        budgetRepository.delete(budget);
    }


    private Budget findBudgetOrThrow(UUID id, User user) {
        return budgetRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", "id", id));
    }

    private BudgetResponse toResponse(Budget budget) {
        BigDecimal spent = budgetRepository.sumSpentByUserAndCategoryAndMonthAndYear(
                budget.getUser(),
                budget.getCategory(),
                budget.getMonth(),
                budget.getYear());

        BigDecimal limit = budget.getMonthlyLimit();
        BigDecimal remaining = limit.subtract(spent);

        double percentage = limit.compareTo(BigDecimal.ZERO) == 0
                ? 0.0
                : spent.divide(limit, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();

        Category cat = budget.getCategory();

        return BudgetResponse.builder()
                .id(budget.getId())
                .category(CategorySummary.builder()
                        .id(cat.getId())
                        .name(cat.getName())
                        .type(cat.getType())
                        .build())
                .limitAmount(limit)
                .spentAmount(spent)
                .remainingAmount(remaining)
                .percentage(Math.round(percentage * 100.0) / 100.0)
                .month(budget.getMonth())
                .year(budget.getYear())
                .alertSent80(budget.isAlertSent80())
                .alertSent100(budget.isAlertSent100())
                .build();
    }

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }
}