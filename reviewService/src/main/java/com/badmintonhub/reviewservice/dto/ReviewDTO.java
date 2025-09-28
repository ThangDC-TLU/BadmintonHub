package com.badmintonhub.reviewservice.dto;
import com.badmintonhub.reviewservice.entity.Review;
import com.badmintonhub.reviewservice.utils.ReviewStatusEnum;
import java.time.Instant; import java.util.List;

public record ReviewDTO(
        String id, Long productId, String userId,
        int rating, String title, String content,
        List<String> images, ReviewStatusEnum status,
        Instant createdAt, Instant updatedAt
){
    public static ReviewDTO from(Review r){
        return new ReviewDTO(r.getId(), r.getProductId(), r.getUserId(),
                r.getRating(), r.getTitle(), r.getContent(), r.getImages(),
                r.getStatus(), r.getCreatedAt(), r.getUpdatedAt());
    }
}
