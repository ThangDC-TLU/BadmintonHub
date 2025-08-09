package com.badmintonhub.productservice.service.impl;

import com.badmintonhub.productservice.dto.mapper.ProductMapper;
import com.badmintonhub.productservice.dto.message.ObjectResponse;
import com.badmintonhub.productservice.dto.model.ProductDTO;
import com.badmintonhub.productservice.dto.model.ProductOptionDTO;
import com.badmintonhub.productservice.dto.model.ProductSpecificationDTO;
import com.badmintonhub.productservice.dto.response.ProductResponseDTO;
import com.badmintonhub.productservice.entity.Category;
import com.badmintonhub.productservice.entity.Product;
import com.badmintonhub.productservice.entity.ProductOption;
import com.badmintonhub.productservice.entity.ProductSpecification;
import com.badmintonhub.productservice.repository.CategoryRepository;
import com.badmintonhub.productservice.repository.ProductRepository;
import com.badmintonhub.productservice.service.ProductService;
import com.badmintonhub.productservice.utils.format.SlugConvert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    public final CategoryRepository categoryRepository;
    public ProductServiceImpl(ProductRepository productRepository, ProductMapper productMapper, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.categoryRepository = categoryRepository;
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
        Product newProduct = productMapper.mapToEntity(productDTO);

        // Xử lý Category (nếu có categoryUrl)
        if (newProduct.getCategoryUrl() != null && !newProduct.getCategoryUrl().isBlank()) {
            String categoryUrl = SlugConvert.convert(newProduct.getCategoryUrl());
            Category category = this.categoryRepository.findByUrlKey(categoryUrl);
            newProduct.setCategory(category);
        }

        if (this.productRepository.existsByProductSlug(SlugConvert.convert(productDTO.getName()))) {
            throw new IllegalArgumentException("Product slug already exists!");
        }else {
            newProduct.setProductSlug(SlugConvert.convert(productDTO.getName()));
        }


        // Gán options từ DTO
        if (productDTO.getOptions() != null) {
            for (ProductOptionDTO optionDTO : productDTO.getOptions()) {
                ProductOption option = new ProductOption();
                option.setName(optionDTO.getName());
                option.setValue(optionDTO.getValue());
                option.setAddPrice(optionDTO.getAddPrice());
                option.setSubPrice(optionDTO.getSubPrice());
                option.setProduct(newProduct);
                newProduct.addOption(option);
            }
        }

        // Gán specifications từ DTO
        if (productDTO.getSpecifications() != null) {
            for (ProductSpecificationDTO specDTO : productDTO.getSpecifications()) {
                ProductSpecification spec = new ProductSpecification();
                spec.setName(specDTO.getName());
                spec.setValue(specDTO.getValue());
                spec.setProduct(newProduct);
                newProduct.addSpecification(spec);
            }
        }

        // Lưu sản phẩm
        Product productResponse = this.productRepository.save(newProduct);

        // Trả về DTO phản hồi
        ProductResponseDTO pr = productMapper.mapToResponse(productResponse);
        return pr;

    }
}
