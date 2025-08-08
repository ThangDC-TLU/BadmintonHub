package com.badmintonhub.productservice.service.impl;

import com.badmintonhub.productservice.entity.Product;
import com.badmintonhub.productservice.repository.ProductRepository;
import com.badmintonhub.productservice.service.ProductService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public List<Product> fetchAllProduct() {
        return this.productRepository.findAll();
    }
}
