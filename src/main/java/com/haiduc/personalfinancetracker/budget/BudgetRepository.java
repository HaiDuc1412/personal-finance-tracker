package com.haiduc.personalfinancetracker.budget;

import com.haiduc.personalfinancetracker.category.Category;
import com.haiduc.personalfinancetracker.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetRepository extends JpaRepository<Budget, UUID> {
    List<Budget> findAllByUserAndMonthAndYear(User user, Short month, Short year);

    boolean existsByUserAndCategoryAndMonthAndYear(
            User user, Category category, Short month, Short year);

    boolean existsByUserAndCategoryAndMonthAndYearAndIdNot(
            User user, Category category, Short month, Short year, UUID id);

    Optional<Budget> findByIdAndUser(UUID id, User user);

    @Query("""
        SELECT COALESCE(SUM(t.amount), 0)
        FROM Transaction t
        WHERE t.user = :user
          AND t.category = :category
          AND t.isDeleted = false
          AND FUNCTION('MONTH', t.transactionDate) = :month
          AND FUNCTION('YEAR', t.transactionDate) = :year
        """)
    BigDecimal sumSpentByUserAndCategoryAndMonthAndYear(
            @Param("user") User user,
            @Param("category") Category category,
            @Param("month") Short month,
            @Param("year") Short year);
}
