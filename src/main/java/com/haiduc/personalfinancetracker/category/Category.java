package com.haiduc.personalfinancetracker.category;

import com.haiduc.personalfinancetracker.common.BaseEntity;
import com.haiduc.personalfinancetracker.common.enums.TransactionType;
import com.haiduc.personalfinancetracker.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 50)
    private String icon;

    @Column(length = 7)
    private String color;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TransactionType type;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;

    // nullable = true: system categories có user = null
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;
}
