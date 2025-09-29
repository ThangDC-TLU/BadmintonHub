package com.badmintonhub.reviewservice.events;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaRatingEventPublisher{

    private final KafkaTemplate<String, ProductRatingUpdated> kafkaTemplate;

    @Value("${kafka.topics.productRatingUpdated}")
    private String topic;

    public void publish(ProductRatingUpdated event) {
        String key = String.valueOf(event.getProductId());
        kafkaTemplate.send(topic, key, event)
                .whenComplete((res, ex) -> {
                    if (ex != null) {
                        log.error("❌ Kafka publish failed: {}", ex.getMessage(), ex);
                    } else {
                        log.info("📤 Kafka published: topic={}, key={}, avg={}, cnt={}",
                                topic, key, event.getRatingAverage(), event.getReviewCount());
                    }
                });
    }
}
