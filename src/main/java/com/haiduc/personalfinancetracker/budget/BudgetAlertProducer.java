package com.haiduc.personalfinancetracker.budget;

import com.haiduc.personalfinancetracker.budget.event.BudgetAlertEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BudgetAlertProducer {

    private static final String TOPIC = "budget-alerts";
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendAlert(BudgetAlertEvent event) {
        kafkaTemplate.send(TOPIC, event.getUserId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to send budget alert: userId={}, type={}, error={}",
                                event.getUserId(), event.getAlertType(), ex.getMessage());
                    } else {
                        log.info("Budget alert sent: userId={}, type={}, category={}",
                                event.getUserId(), event.getAlertType(), event.getCategoryName());
                    }
                });
    }
}
