package com.badmintonhub.productservice.dto.response;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class CategoryResponseDTO {
    private Long id;
    private String name;
    private String urlKey;
    private String thumbnailUrl;
    private Long parentId;
}
