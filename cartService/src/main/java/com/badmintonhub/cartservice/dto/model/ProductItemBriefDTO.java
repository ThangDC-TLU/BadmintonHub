package com.badmintonhub.cartservice.dto.model;

import lombok.Data;


@Data
public class ProductItemBriefDTO {
    private Long productId;
    private Long optionId;
    private Long skuId;
    private String name;           // tên sản phẩm
    private String image;          // thumbnail
    private String productSlug;
    private String optionLabel;    // ví dụ "Size M / Red"
    private Boolean available;     // còn bán được không (optional)
    private int quantity;
    private double basePrice;          // giá gốc product
    private double addPrice;           // cộng theo option
    private double subPrice;           // trừ theo option
    private double discountPercent;    // 0..100
    private double finalPrice;         // base + add - sub
    private double discountedFinalPrice; // finalPrice * (1 - discount)

    private double lineFinalPrice;           // finalPrice * quantity
    private double lineDiscountedFinalPrice; // discountedFinalPrice * quantity
}