package com.badmintonhub.productservice.service;

import com.badmintonhub.productservice.dto.message.ObjectResponse;
import com.badmintonhub.productservice.dto.model.ProductDTO;
import com.badmintonhub.productservice.dto.response.ProductResponseDTO;
import com.badmintonhub.productservice.entity.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface ProductService {

    ObjectResponse getAllProduct(Specification<Product> specification, Pageable pageable);

    ProductResponseDTO createProduct(ProductDTO productDTO);

}
