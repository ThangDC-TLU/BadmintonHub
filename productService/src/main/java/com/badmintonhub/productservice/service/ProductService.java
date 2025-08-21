package com.badmintonhub.productservice.service;

import com.badmintonhub.productservice.dto.message.ObjectResponse;
import com.badmintonhub.productservice.dto.model.ProductItemBriefDTO;
import com.badmintonhub.productservice.dto.model.ProductDTO;
import com.badmintonhub.productservice.dto.model.ProductUpdateDTO;
import com.badmintonhub.productservice.dto.response.ProductResponseDTO;
import com.badmintonhub.productservice.entity.Product;
import com.badmintonhub.productservice.exception.IdInvalidException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;

public interface ProductService {

    ObjectResponse getAllProduct(Specification<Product> specification, Pageable pageable);

    ProductResponseDTO createProduct(ProductDTO productDTO) throws IdInvalidException;


    ProductResponseDTO getProductById(Long productId);

//    ProductResponseDTO updateProduct(Long productId, @Valid ProductDTO productDTO);

    void deleteProduct(Long productId);

    ProductResponseDTO updateStock(Long productId, int quantity);

    ProductResponseDTO updateProduct(Long productId, @Valid ProductUpdateDTO productUpdateDTO) throws IdInvalidException;

    Map<Long, ProductItemBriefDTO> getProductItemBriefByOptionIds(List<Long> optionIds);
}
