package com.badmintonhub.productservice.dto.model;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;
import java.util.Set;


@Data
public class ProductUpdateDTO {
    private Long id;

    @NotEmpty(message = "Description should not be empty")
    @Size(min = 10, message = "Product Description should have at least 2 characters!")
    private String description;

    @NotNull(message = "Price should not be empty")
    @Positive(message = "Price must be greater than zero")
    private double price;

    private double discountRate;

    @NotEmpty(message = "thumbnail should not be empty")
    private String thumbnailUrl;
    private int reviewCount;
    private double ratingAverage;
    private int quantityStock;
    private int quantitySold;

    private String categoryUrl;

    private List<ProductOptionDTO> options;
    private Set<ProductSpecificationDTO> specifications;
}

