package com.badmintonhub.productservice.events;

import lombok.*;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRatingUpdated {
    Long productId;
    double ratingAverage;
    int reviewCount;
    Instant occurredAt;
    String eventId;
}
