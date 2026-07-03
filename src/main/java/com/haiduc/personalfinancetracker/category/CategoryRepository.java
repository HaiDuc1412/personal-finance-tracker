package com.haiduc.personalfinancetracker.category;

import com.haiduc.personalfinancetracker.common.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    Optional<Category> findByNameAndUserIsNull(String name);

    @Query("SELECT c FROM Category c WHERE c.user IS NULL OR c.user.id = :userId")
    List<Category> findAllAvailableForUser(@Param("userId") UUID userId);

    // Filter thêm theo type
    @Query("SELECT c FROM Category c WHERE (c.user IS NULL OR c.user.id = :userId) AND c.type = :type")
    List<Category> findAllAvailableForUserByType(@Param("userId") UUID userId,
            @Param("type") TransactionType type);
}
