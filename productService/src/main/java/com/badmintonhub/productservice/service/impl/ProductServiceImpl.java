package com.badmintonhub.productservice.service.impl;

import com.badmintonhub.productservice.dto.mapper.ProductMapper;
import com.badmintonhub.productservice.dto.message.ObjectResponse;
import com.badmintonhub.productservice.dto.model.ProductDTO;
import com.badmintonhub.productservice.dto.response.CategoryResponseDTO;
import com.badmintonhub.productservice.dto.response.ProductResponseDTO;
import com.badmintonhub.productservice.entity.Product;
import com.badmintonhub.productservice.repository.ProductRepository;
import com.badmintonhub.productservice.service.ProductService;
import com.badmintonhub.productservice.utils.format.SlugConvert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    public ProductServiceImpl(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Override
    public ObjectResponse getAllProduct(Specification<Product> specification, Pageable pageable) {
        Page<Product> productPage = this.productRepository.findAll(specification, pageable);
        ObjectResponse objectResponse = new ObjectResponse();
        ObjectResponse.Meta meta = new ObjectResponse.Meta();

        meta.setTotal(productPage.getTotalElements());
        meta.setPages(productPage.getTotalPages());

        meta.setPageSize(pageable.getPageSize());
        meta.setPage(pageable.getPageNumber() + 1);

        objectResponse.setMeta(meta);
        List<ProductResponseDTO> productResponseDTOList = productPage
                .getContent()
                .stream()
                .map(productMapper::mapToResponse)
                .toList();

        objectResponse.setResult(productResponseDTOList);
        return objectResponse;
    }

    @Override
    public ProductResponseDTO createProduct(ProductDTO productDTO) {


    }
}
