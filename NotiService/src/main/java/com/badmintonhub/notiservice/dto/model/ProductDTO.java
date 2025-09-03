package com.badmintonhub.notiservice.dto.model;

import lombok.Data;

@Data
public class ProductDTO {
    private String name;
    private double price;
    private double discountRate;
}
