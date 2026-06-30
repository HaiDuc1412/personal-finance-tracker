package com.haiduc.personalfinancetracker.budget;

import com.haiduc.personalfinancetracker.budget.event.BudgetAlertEvent;

public interface EmailService {
    void sendBudgetAlert(BudgetAlertEvent event);
}
