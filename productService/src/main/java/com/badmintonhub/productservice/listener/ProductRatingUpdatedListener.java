package com.badmintonhub.productservice.listener;

import com.badmintonhub.productservice.events.ProductRatingUpdated;
import com.badmintonhub.productservice.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@Component
public class ProductRatingUpdatedListener {
    private final ProductRepository productRepository;

    public ProductRatingUpdatedListener(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    private static double round1(double v){ return Math.round(v*10.0)/10.0; }

    @Transactional
    @KafkaListener(topics = "product-rating-updated", groupId = "product-group", containerFactory = "productRatingUpdatedConcurrentKafkaListenerContainerFactory")
    public void handleOrderEvent(ProductRatingUpdated evt) {
        log.info("Nhận được event từ Kafka: " + evt);
        Long productId = evt.getProductId();
        double avg = round1(evt.getRatingAverage());
        int cnt = evt.getReviewCount();
        Instant occurredAt = Optional.ofNullable(evt.getOccurredAt()).orElse(Instant.now());

        // last-write-wins
        Instant current = productRepository.getRatingUpdatedAt(productId);
        if (current != null && current.isAfter(occurredAt)) {
            log.info("Skip outdated rating event for product {} (eventAt={}, currentAt={})",
                    productId, occurredAt, current);
            return;
        }

        int rows = productRepository.updateRatingAndCount(productId, avg, cnt, occurredAt);
        if (rows == 0) {
            log.warn("Product {} not found while updating rating (eventId={})",
                    productId, evt.getEventId());
        } else {
            log.info("Updated product {} => ratingAverage={}, reviewCount={} (eventId={})",
                    productId, avg, cnt, evt.getEventId());
        }
    }
}
