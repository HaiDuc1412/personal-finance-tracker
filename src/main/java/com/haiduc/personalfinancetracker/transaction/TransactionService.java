package com.haiduc.personalfinancetracker.transaction;

import com.haiduc.personalfinancetracker.category.Category;
import com.haiduc.personalfinancetracker.category.CategoryRepository;
import com.haiduc.personalfinancetracker.common.enums.TransactionType;
import com.haiduc.personalfinancetracker.common.exception.AppException;
import com.haiduc.personalfinancetracker.transaction.dto.CategorySummary;
import com.haiduc.personalfinancetracker.transaction.dto.TransactionRequest;
import com.haiduc.personalfinancetracker.transaction.dto.TransactionResponse;
import com.haiduc.personalfinancetracker.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;

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

        Pageable pageable = PageRequest.of(page, size);

        return transactionRepository.findAllByFilter(
                currentUser.getId(),
                type != null ? type.name() : "EXPENSE",
                type == null,
                categoryId != null ? categoryId.toString() : "",
                categoryId == null,
                fromDate != null ? fromDate : LocalDate.now(),
                fromDate == null,
                toDate != null ? toDate : LocalDate.now(),
                toDate == null,
                pageable
        ).map(this::toResponse);
    }

    @Transactional
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

        return toResponse(transactionRepository.save(transaction));
    }

    @Transactional
    public TransactionResponse updateTransaction(User currentUser, UUID transactionId, TransactionRequest request) {
        Transaction transaction = getOwnedTransactionOrThrow(currentUser, transactionId);
        Category category = getAccessibleCategoryOrThrow(currentUser, request.getCategoryId());

        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setCategory(category);
        transaction.setType(category.getType());

        return toResponse(transactionRepository.save(transaction));
    }

    @Transactional
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
}
