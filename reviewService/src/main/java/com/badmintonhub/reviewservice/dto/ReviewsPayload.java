package com.badmintonhub.reviewservice.dto;

import java.util.List;
public record ReviewsPayload(
        List<ReviewDTO> items,
        double ratingAverage,
        int reviewCount
) {}
