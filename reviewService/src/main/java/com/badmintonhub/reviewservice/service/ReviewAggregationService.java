package com.badmintonhub.reviewservice.service;


import com.badmintonhub.reviewservice.dto.ProductAgg;
import com.badmintonhub.reviewservice.events.ProductRatingUpdated;
import com.badmintonhub.reviewservice.events.KafkaRatingEventPublisher;
import com.badmintonhub.reviewservice.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewAggregationService {

    private final ReviewRepository repo;
    private final KafkaRatingEventPublisher publisher;

    /** Tính lại avg/count và publish event */
    public void recomputeAndPublish(Long productId) {
        List<ProductAgg> agg = repo.aggProduct(productId);
        double avg = agg.isEmpty() ? 0.0 : agg.get(0).getAvgRating();
        int cnt    = agg.isEmpty() ? 0   : agg.get(0).getCount();

        ProductRatingUpdated event = ProductRatingUpdated.builder()
                .productId(productId)
                .ratingAverage(avg)
                .reviewCount(cnt)
                .occurredAt(Instant.now())
                .eventId(UUID.randomUUID().toString())
                .build();

        publisher.publish(event);
    }
}
