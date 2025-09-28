package com.badmintonhub.reviewservice.dto;
import jakarta.validation.constraints.*;

public record UpdateReview(
        @Min(1) @Max(5) int rating,
        @Size(max=120) String title,
        @Size(max=3000) String content
) {}