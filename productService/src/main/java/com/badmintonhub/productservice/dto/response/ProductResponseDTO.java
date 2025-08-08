package com.badmintonhub.productservice.dto.response;


import lombok.Data;

@Data
public class ProductResponseDTO {
    private Long id;

    private String name;
    private String brand;
    private double price;

    private double discountRate;
    private String thumbnailUrl;
    private int reviewCount;
    private double ratingAverage;
    private int quantitySold;
    private String productSlug;
}

