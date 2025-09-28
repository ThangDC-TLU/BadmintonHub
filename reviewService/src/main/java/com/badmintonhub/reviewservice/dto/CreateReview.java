package com.badmintonhub.reviewservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateReview(
        @NotNull Long productId,
        @Min(1) @Max(5) int rating,
        @Size(max=120) String title,
        @Size(max=3000) String content,
        @Size(max=10) List<@Size(max=512) String> images
) {}
