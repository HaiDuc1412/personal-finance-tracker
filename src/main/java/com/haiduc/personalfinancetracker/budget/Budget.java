package com.haiduc.personalfinancetracker.budget;

import com.haiduc.personalfinancetracker.category.Category;
import com.haiduc.personalfinancetracker.common.BaseEntity;
import com.haiduc.personalfinancetracker.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "budgets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Budget extends BaseEntity {

    @Column(name = "monthly_limit", nullable = false, precision = 15, scale = 2)
    private BigDecimal monthlyLimit;

    @Column(nullable = false)
    private Short month;

    @Column(nullable = false)
    private Short year;

    @Builder.Default
    @Column(name = "alert_sent_80", nullable = false)
    private boolean alertSent80 = false;

    @Builder.Default
    @Column(name = "alert_sent_100", nullable = false)
    private boolean alertSent100 = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
}
