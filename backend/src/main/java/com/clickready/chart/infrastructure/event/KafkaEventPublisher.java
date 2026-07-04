package com.clickready.chart.infrastructure.event;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import com.clickready.chart.application.port.out.EventPublisherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventPublisher implements EventPublisherPort {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC = "chart-events";

    @Override
    public <T> void publish(T event) {
        try {
            log.debug("Публикация события: {}", event);
            kafkaTemplate.send(TOPIC, event);
            log.debug("Событие опубликовано в Kafka");
        } catch (Exception e) {
            log.error("Ошибка при публикации события в Kafka: {}", e.getMessage(), e);
            // Можно добавить fallback или повторную попытку
        }
    }
}