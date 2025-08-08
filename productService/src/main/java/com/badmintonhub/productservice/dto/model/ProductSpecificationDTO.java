package com.badmintonhub.productservice.dto.model;

import lombok.Data;

@Data
public class ProductSpecificationDTO {
    private Long id;

    private String name;
    private String value;
}