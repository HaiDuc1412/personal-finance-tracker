package com.haiduc.personalfinancetracker.transaction;

import com.haiduc.personalfinancetracker.common.enums.TransactionType;
import com.haiduc.personalfinancetracker.user.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID>, JpaSpecificationExecutor<Transaction> {

  @Modifying
  @Query("UPDATE Transaction t SET t.category.id = :newCategoryId WHERE t.category.id = :oldCategoryId")
  void reassignCategory(@Param("oldCategoryId") UUID oldCategoryId,
      @Param("newCategoryId") UUID newCategoryId);

  // Tổng income/expense theo tháng
  @Query("""
      SELECT COALESCE(SUM(t.amount), 0)
      FROM Transaction t
      WHERE t.user = :user
        AND t.isDeleted = false
        AND t.type = :type
        AND EXTRACT(MONTH FROM t.transactionDate) = :month
        AND EXTRACT(YEAR FROM t.transactionDate) = :year
      """)
  BigDecimal sumByUserAndTypeAndMonthAndYear(
      @Param("user") User user,
      @Param("type") TransactionType type,
      @Param("month") int month,
      @Param("year") int year);

  // 5 giao dịch gần nhất
  @Query("""
      SELECT t FROM Transaction t
      WHERE t.user = :user
        AND t.isDeleted = false
        AND EXTRACT(MONTH FROM t.transactionDate) = :month
        AND EXTRACT(YEAR FROM t.transactionDate) = :year
      ORDER BY t.transactionDate DESC, t.createdAt DESC
      """)
  List<Transaction> findTop5ByUserAndMonthAndYear(
      @Param("user") User user,
      @Param("month") int month,
      @Param("year") int year,
      Pageable pageable);

  // Expense theo từng category trong tháng
  @Query("""
      SELECT t.category.name, COALESCE(SUM(t.amount), 0)
      FROM Transaction t
      WHERE t.user = :user
        AND t.isDeleted = false
        AND t.type = 'EXPENSE'
        AND EXTRACT(MONTH FROM t.transactionDate) = :month
        AND EXTRACT(YEAR FROM t.transactionDate) = :year
      GROUP BY t.category.name
      ORDER BY SUM(t.amount) DESC
      """)
  List<Object[]> findExpenseGroupedByCategoryAndMonth(
      @Param("user") User user,
      @Param("month") int month,
      @Param("year") int year);
}
