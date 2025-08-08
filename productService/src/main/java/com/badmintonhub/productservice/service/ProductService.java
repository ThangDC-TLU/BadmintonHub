package com.badmintonhub.productservice.service;

import com.badmintonhub.productservice.entity.Product;

import java.util.List;

public interface ProductService {
    List<Product> fetchAllProduct();
}
