package com.haiduc.personalfinancetracker.transaction;

import com.haiduc.personalfinancetracker.budget.BudgetAlertProducer;
import com.haiduc.personalfinancetracker.budget.BudgetRepository;
import com.haiduc.personalfinancetracker.budget.event.BudgetAlertEvent;
import com.haiduc.personalfinancetracker.category.Category;
import com.haiduc.personalfinancetracker.category.CategoryRepository;
import com.haiduc.personalfinancetracker.common.enums.TransactionType;
import com.haiduc.personalfinancetracker.common.exception.AppException;
import com.haiduc.personalfinancetracker.transaction.dto.CategorySummary;
import com.haiduc.personalfinancetracker.transaction.dto.TransactionRequest;
import com.haiduc.personalfinancetracker.transaction.dto.TransactionResponse;
import com.haiduc.personalfinancetracker.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final BudgetRepository budgetRepository;
    private final BudgetAlertProducer budgetAlertProducer;

    public Page<TransactionResponse> getTransactions(
            User currentUser,
            TransactionType type,
            UUID categoryId,
            LocalDate fromDate,
            LocalDate toDate,
            int page,
            int size) {

        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new AppException("fromDate must not be after toDate", HttpStatus.BAD_REQUEST);
        }

        if (categoryId != null) {
            categoryRepository.findById(categoryId)
                    .filter(c -> c.getUser() == null
                            || c.getUser().getId().equals(currentUser.getId()))
                    .orElseThrow(() -> new AppException("Category not found", HttpStatus.NOT_FOUND));
        }

        Specification<Transaction> spec = Specification
                .where(TransactionSpecification.ofUser(currentUser))
                .and(TransactionSpecification.notDeleted())
                .and(TransactionSpecification.hasType(type))
                .and(TransactionSpecification.hasCategory(categoryId))
                .and(TransactionSpecification.fromDate(fromDate))
                .and(TransactionSpecification.toDate(toDate));

        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "transactionDate", "createdAt"));

        return transactionRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "dashboard", allEntries = true),
            @CacheEvict(value = "budgets",   allEntries = true)
    })
    public TransactionResponse createTransaction(User currentUser, TransactionRequest request) {
        Category category = getAccessibleCategoryOrThrow(currentUser, request.getCategoryId());

        Transaction transaction = Transaction.builder()
                .amount(request.getAmount())
                .description(request.getDescription())
                .transactionDate(request.getTransactionDate())
                .category(category)
                .type(category.getType())
                .user(currentUser)
                .isDeleted(false)
                .build();
        Transaction saved = transactionRepository.save(transaction);

        // Trigger alert check
        if (saved.getType() == TransactionType.EXPENSE) {
            checkAndPublishBudgetAlert(saved, currentUser);
        }

        return toResponse(saved);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "dashboard", allEntries = true),
            @CacheEvict(value = "budgets",   allEntries = true)
    })
    public TransactionResponse updateTransaction(User currentUser, UUID transactionId, TransactionRequest request) {
        Transaction transaction = getOwnedTransactionOrThrow(currentUser, transactionId);
        Category category = getAccessibleCategoryOrThrow(currentUser, request.getCategoryId());

        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setCategory(category);
        transaction.setType(category.getType());

        Transaction saved = transactionRepository.save(transaction);

        // Trigger alert check
        if (saved.getType() == TransactionType.EXPENSE) {
            checkAndPublishBudgetAlert(saved, currentUser);
        }

        return toResponse(saved);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "dashboard", allEntries = true),
            @CacheEvict(value = "budgets",   allEntries = true)
    })
    public void deleteTransaction(User currentUser, UUID transactionId) {
        Transaction transaction = getOwnedTransactionOrThrow(currentUser, transactionId);
        transaction.setDeleted(true);
        transactionRepository.save(transaction);
    }

    private Category getAccessibleCategoryOrThrow(User currentUser, UUID categoryId) {
        return categoryRepository.findById(categoryId)
                .filter(c -> c.getUser() == null
                        || c.getUser().getId().equals(currentUser.getId()))
                .orElseThrow(() -> new AppException("Category not found", HttpStatus.NOT_FOUND));
    }

    private Transaction getOwnedTransactionOrThrow(User currentUser, UUID transactionId) {
        return transactionRepository.findById(transactionId)
                .filter(t -> !t.isDeleted())
                .filter(t -> t.getUser().getId().equals(currentUser.getId()))
                .orElseThrow(() -> new AppException("Transaction not found", HttpStatus.NOT_FOUND));
    }

    private TransactionResponse toResponse(Transaction t) {
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

    // ─── Budget Alert Logic ───────────────────────────────────────────────────────

    private void checkAndPublishBudgetAlert(Transaction transaction, User user) {
        budgetRepository.findByUserAndCategoryAndMonthAndYear(
                user,
                transaction.getCategory(),
                (short) transaction.getTransactionDate().getMonthValue(),
                (short) transaction.getTransactionDate().getYear()
        ).ifPresent(budget -> {
            BigDecimal spent = budgetRepository.sumSpentByUserAndCategoryAndMonthAndYear(
                    user,
                    transaction.getCategory(),
                    (short) transaction.getTransactionDate().getMonthValue(),
                    (short) transaction.getTransactionDate().getYear()
            );

            BigDecimal limit = budget.getMonthlyLimit();
            double percentage = limit.compareTo(BigDecimal.ZERO) == 0 ? 0.0
                    : spent.divide(limit, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();

            // Kiểm tra 80% trước – gửi nếu chưa từng gửi
            if (percentage >= 80.0 && !budget.isAlertSent80()) {
                budget.setAlertSent80(true);
                budgetAlertProducer.sendAlert(BudgetAlertEvent.builder()
                        .userId(user.getId())
                        .userEmail(user.getEmail())
                        .categoryName(budget.getCategory().getName())
                        .limitAmount(limit)
                        .spentAmount(spent)
                        .percentage(Math.round(percentage * 100.0) / 100.0)
                        .alertType(BudgetAlertEvent.AlertType.ALERT_80)
                        .month(budget.getMonth())
                        .year(budget.getYear())
                        .build());
            }

            // Kiểm tra 100% độc lập – gửi nếu chưa từng gửi
            if (percentage >= 100.0 && !budget.isAlertSent100()) {
                budget.setAlertSent100(true);
                budgetAlertProducer.sendAlert(BudgetAlertEvent.builder()
                        .userId(user.getId())
                        .userEmail(user.getEmail())
                        .categoryName(budget.getCategory().getName())
                        .limitAmount(limit)
                        .spentAmount(spent)
                        .percentage(Math.round(percentage * 100.0) / 100.0)
                        .alertType(BudgetAlertEvent.AlertType.ALERT_100)
                        .month(budget.getMonth())
                        .year(budget.getYear())
                        .build());
            }

            // Lưu budget một lần sau khi cập nhật cả 2 flag
            budgetRepository.save(budget);
        });
    }
}
