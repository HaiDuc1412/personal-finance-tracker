package com.haiduc.personalfinancetracker.transaction;

import com.haiduc.personalfinancetracker.category.Category;
import com.haiduc.personalfinancetracker.common.enums.TransactionType;
import com.haiduc.personalfinancetracker.user.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;

public class TransactionSpecification {

    private TransactionSpecification() {}

    // Trả về Specification luôn TRUE (no-op) — tương đương bỏ qua điều kiện
    private static <T> Specification<T> noOp() {
        return (root, query, cb) -> cb.conjunction();
    }

    public static Specification<Transaction> ofUser(User user) {
        return (root, query, cb) ->
                cb.equal(root.get("user"), user);
    }

    public static Specification<Transaction> notDeleted() {
        return (root, query, cb) ->
                cb.isFalse(root.get("isDeleted"));
    }

    public static Specification<Transaction> hasType(TransactionType type) {
        if (type == null) return noOp();
        return (root, query, cb) -> {
            Join<Transaction, Category> category = root.join("category", JoinType.INNER);
            return cb.equal(category.get("type"), type);
        };
    }

    public static Specification<Transaction> hasCategory(UUID categoryId) {
        if (categoryId == null) return noOp();
        return (root, query, cb) ->
                cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Transaction> fromDate(LocalDate fromDate) {
        if (fromDate == null) return noOp();
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("transactionDate"), fromDate);
    }

    public static Specification<Transaction> toDate(LocalDate toDate) {
        if (toDate == null) return noOp();
        return (root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("transactionDate"), toDate);
    }

    public static Specification<Transaction> inMonth(Integer month) {
        if (month == null) return noOp();
        return (root, query, cb) ->
                cb.equal(((HibernateCriteriaBuilder) cb).month(root.get("transactionDate")), month);
    }

    public static Specification<Transaction> inYear(Integer year) {
        if (year == null) return noOp();
        return (root, query, cb) ->
                cb.equal(((HibernateCriteriaBuilder) cb).year(root.get("transactionDate")), year);
    }
}
