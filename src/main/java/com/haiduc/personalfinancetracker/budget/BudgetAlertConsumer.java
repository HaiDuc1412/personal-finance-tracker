package com.haiduc.personalfinancetracker.budget;

import com.haiduc.personalfinancetracker.budget.event.BudgetAlertEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BudgetAlertConsumer {

    private final EmailService emailService;

    @KafkaListener(
            topics = "budget-alerts",
            groupId = "pft-budget-alert-group",
            containerFactory = "budgetAlertKafkaListenerContainerFactory"
    )
    public void consume(BudgetAlertEvent event) {
        log.info("Received budget alert: userId={}, type={}, category={}, percentage={}%",
                event.getUserId(), event.getAlertType(),
                event.getCategoryName(), event.getPercentage());
        emailService.sendBudgetAlert(event);
    }
}
