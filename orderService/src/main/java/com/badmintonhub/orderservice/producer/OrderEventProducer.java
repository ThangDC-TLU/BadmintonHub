package com.badmintonhub.orderservice.producer;

import com.badmintonhub.orderservice.dto.event.OrderCompletedEvent;
import com.badmintonhub.orderservice.dto.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC_ORDER_CREATED = "orders";

    public void publishOrderCreatedEvent(OrderCreatedEvent event) {
        kafkaTemplate.send(TOPIC_ORDER_CREATED, String.valueOf(event.getOrderId()), event)
                .whenComplete((res, ex) -> {
                    if (ex != null) {
                        log.error("❌ Kafka send failed (orders, id={}): {}", event.getOrderId(), ex.getMessage(), ex);
                    } else {
                        var md = res.getRecordMetadata();
                        log.info("✅ Kafka sent (orders) id={} -> {}-{}@{}", event.getOrderId(), md.topic(), md.partition(), md.offset());
                    }
                });
    }
}

