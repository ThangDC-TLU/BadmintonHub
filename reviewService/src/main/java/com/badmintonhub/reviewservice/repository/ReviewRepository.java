package com.badmintonhub.reviewservice.repository;

import com.badmintonhub.reviewservice.entity.Review;
import org.springframework.data.domain.*; import org.springframework.data.mongodb.repository.*; import java.util.*;

public interface ReviewRepository extends MongoRepository<Review, String> {
    Page<Review> findByProductIdAndStatus(Long productId, String status, Pageable pageable);
    Optional<Review> findByProductIdAndUserId(Long productId, String userId);

    @Aggregation(pipeline = {
            "{ $match: { productId: ?0, status: 'APPROVED' } }",
            "{ $group: { _id: '$productId', avgRating: { $avg: '$rating' }, count: { $sum: 1 } } }"
    })
    List<ProductAgg> aggProduct(Long productId);
    interface ProductAgg { String get_id(); Double getAvgRating(); Integer getCount(); }
}
