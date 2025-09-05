package com.badmintonhub.notiservice.listener;

import com.badmintonhub.notiservice.dto.event.OrderPlacedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderEventListener {

    @KafkaListener(topics = "order-topic", groupId = "notification-group", containerFactory = "orderPlacedEventListenerFactory")
    public void handleOrderEvent(OrderPlacedEvent event) {
        log.info("Nhận được event từ Kafka: " + event);
        // Thực hiện gửi email ở đây

    }
}
