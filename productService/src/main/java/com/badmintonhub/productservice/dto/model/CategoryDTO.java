package com.badmintonhub.productservice.dto.model;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Set;

@Data
public class CategoryDTO {
    @NotEmpty(message = "Name should not be empty")
    private String name;
    private String urlKey;
    private String thumbnailUrl;
    private Long parentId;
}
