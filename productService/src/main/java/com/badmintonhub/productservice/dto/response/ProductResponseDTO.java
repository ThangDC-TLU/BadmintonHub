package com.badmintonhub.productservice.dto.response;


import com.badmintonhub.productservice.dto.model.ProductOptionDTO;
import com.badmintonhub.productservice.dto.model.ProductSpecificationDTO;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class ProductResponseDTO {
    private Long id;
    private Long skuId;
    private String name;
    private String brand;
    private String description;
    private double price;
    private double discountRate;
    private String thumbnailUrl;
    private int reviewCount;
    private double ratingAverage;
    private String productSlug;
    private String categoryUrl;
    private List<ProductOptionDTO> options;
    private Set<ProductSpecificationDTO> specifications;

}

