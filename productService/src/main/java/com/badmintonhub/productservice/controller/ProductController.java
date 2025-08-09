package com.badmintonhub.productservice.controller;

import com.badmintonhub.productservice.dto.message.ObjectResponse;
import com.badmintonhub.productservice.dto.model.ProductDTO;
import com.badmintonhub.productservice.dto.response.ProductResponseDTO;
import com.badmintonhub.productservice.entity.Product;
import com.badmintonhub.productservice.service.ProductService;
import com.badmintonhub.productservice.utils.anotation.ApiMessage;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {
    private final ProductService productService;
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @ApiMessage("Get all products")
    public ResponseEntity<ObjectResponse> getAllProducts(
            @Filter Specification<Product> spec,
            Pageable pageable
            ) {
        return ResponseEntity.ok(this.productService.getAllProduct(spec, pageable));
    }

    @PostMapping
    @ApiMessage("Create a product")
    public ResponseEntity<ProductResponseDTO> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.productService.createProduct(productDTO));
    }
}
