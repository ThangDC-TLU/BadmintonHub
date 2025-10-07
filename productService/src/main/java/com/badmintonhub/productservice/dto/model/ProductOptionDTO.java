package com.badmintonhub.productservice.dto.model;

import lombok.Data;

import java.util.List;

@Data
public class ProductOptionDTO {
    private Long id;
    private Long skuId;
    private String name;
    private String value;
    private double addPrice;
    private double subPrice;
}
