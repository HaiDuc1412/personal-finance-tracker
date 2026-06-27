package com.haiduc.personalfinancetracker.transaction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    @Modifying
    @Query("UPDATE Transaction t SET t.category.id = :newCategoryId WHERE t.category.id = :oldCategoryId")
    void reassignCategory(@Param("oldCategoryId") UUID oldCategoryId,
                          @Param("newCategoryId") UUID newCategoryId);

    @Query(value = """
        SELECT t.* FROM transactions t
        JOIN categories c ON c.id = t.category_id
        WHERE t.user_id = CAST(:userId AS UUID)
          AND t.is_deleted = false
          AND (:ignoreType = true OR c.type = :type)
          AND (:ignoreCategoryId = true OR c.id = CAST(:categoryId AS UUID))
          AND (:ignoreFromDate = true OR t.transaction_date >= CAST(:fromDate AS DATE))
          AND (:ignoreToDate = true OR t.transaction_date <= CAST(:toDate AS DATE))
        ORDER BY t.transaction_date DESC, t.created_at DESC
        """,
            countQuery = """
        SELECT COUNT(*) FROM transactions t
        JOIN categories c ON c.id = t.category_id
        WHERE t.user_id = CAST(:userId AS UUID)
          AND t.is_deleted = false
          AND (:ignoreType = true OR c.type = :type)
          AND (:ignoreCategoryId = true OR c.id = CAST(:categoryId AS UUID))
          AND (:ignoreFromDate = true OR t.transaction_date >= CAST(:fromDate AS DATE))
          AND (:ignoreToDate = true OR t.transaction_date <= CAST(:toDate AS DATE))
        """,
            nativeQuery = true)
    Page<Transaction> findAllByFilter(
            @Param("userId") UUID userId,
            @Param("type") String type,
            @Param("ignoreType") boolean ignoreType,
            @Param("categoryId") String categoryId,
            @Param("ignoreCategoryId") boolean ignoreCategoryId,
            @Param("fromDate") LocalDate fromDate,
            @Param("ignoreFromDate") boolean ignoreFromDate,
            @Param("toDate") LocalDate toDate,
            @Param("ignoreToDate") boolean ignoreToDate,
            Pageable pageable);
}
