package com.badmintonhub.inventoryservice.consumer;

import com.badmintonhub.inventoryservice.dto.event.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderEventsListener {
    @KafkaListener(topics = "orders", groupId = "inventory-group", containerFactory = "orderCreatedEventListenerFactory")
    public void handleOrderEvent(OrderCreatedEvent event) {
        log.info("Nhận được event từ Kafka: " + event);
    }
}
