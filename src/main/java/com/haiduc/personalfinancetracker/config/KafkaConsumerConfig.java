package com.haiduc.personalfinancetracker.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.haiduc.personalfinancetracker.budget.event.BudgetAlertEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Bean
    public ConsumerFactory<String, BudgetAlertEvent> budgetAlertConsumerFactory() {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

        Deserializer<BudgetAlertEvent> valueDeserializer = (topic, data) -> {
            if (data == null) return null;
            try {
                return objectMapper.readValue(data, BudgetAlertEvent.class);
            } catch (Exception e) {
                throw new RuntimeException("Failed to deserialize BudgetAlertEvent", e);
            }
        };

        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), valueDeserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, BudgetAlertEvent>
            budgetAlertKafkaListenerContainerFactory(
                    ConsumerFactory<String, BudgetAlertEvent> budgetAlertConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, BudgetAlertEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(budgetAlertConsumerFactory);
        return factory;
    }
}
