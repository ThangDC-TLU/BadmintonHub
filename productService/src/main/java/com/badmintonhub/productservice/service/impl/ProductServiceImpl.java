package com.badmintonhub.productservice.service.impl;

import com.badmintonhub.productservice.dto.mapper.ProductMapper;
import com.badmintonhub.productservice.dto.message.ObjectResponse;
import com.badmintonhub.productservice.dto.model.*;
import com.badmintonhub.productservice.dto.response.ProductResponseDTO;
import com.badmintonhub.productservice.entity.Category;
import com.badmintonhub.productservice.entity.Product;
import com.badmintonhub.productservice.entity.ProductOption;
import com.badmintonhub.productservice.entity.ProductSpecification;
import com.badmintonhub.productservice.exception.IdInvalidException;
import com.badmintonhub.productservice.exception.ResourceNotFoundException;
import com.badmintonhub.productservice.repository.CategoryRepository;
import com.badmintonhub.productservice.repository.ProductOptionRepository;
import com.badmintonhub.productservice.repository.ProductRepository;
import com.badmintonhub.productservice.service.ProductService;
import com.badmintonhub.productservice.utils.format.SlugConvert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    public final CategoryRepository categoryRepository;
    public final ProductOptionRepository productOptionRepository;
    public ProductServiceImpl(ProductRepository productRepository, ProductMapper productMapper, CategoryRepository categoryRepository, ProductOptionRepository productOptionRepository) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.categoryRepository = categoryRepository;
        this.productOptionRepository = productOptionRepository;
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
    public ProductResponseDTO createProduct(ProductDTO productDTO) throws IdInvalidException {
        Product newProduct = productMapper.mapToEntity(productDTO);

        // Xử lý Category (nếu có categoryUrl)
        if (newProduct.getCategoryUrl() != null && !newProduct.getCategoryUrl().isBlank()) {
            String categoryUrl = SlugConvert.convert(newProduct.getCategoryUrl());
            Category category = categoryRepository.findByUrlKey(categoryUrl)
                    .orElseThrow(() -> new IdInvalidException(
                            "Category not found with url: " + categoryUrl
                    ));
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

    @Override
    public ProductResponseDTO getProductById(Long productId) {
        Product product = this.productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product","productId", productId));
        return this.productMapper.mapToResponse(product);
    }

    @Override
    public ProductResponseDTO updateProduct(Long productId, ProductUpdateDTO productDTO) throws IdInvalidException {
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product","productId", productId));

        // Cập nhật mô tả
        existingProduct.setDescription(productDTO.getDescription());

        // Giá + giảm giá
        existingProduct.setPrice(productDTO.getPrice());
        existingProduct.setDiscountRate(productDTO.getDiscountRate());

        // Ảnh thumbnail
        existingProduct.setThumbnailUrl(productDTO.getThumbnailUrl());

        // Review, rating
        existingProduct.setReviewCount(productDTO.getReviewCount());
        existingProduct.setRatingAverage(productDTO.getRatingAverage());

        // Số lượng tồn kho và đã bán
        existingProduct.setQuantityStock(productDTO.getQuantityStock());
        existingProduct.setQuantitySold(productDTO.getQuantitySold());

        // Xử lý Category (nếu có categoryUrl)
        if (productDTO.getCategoryUrl() != null && !productDTO.getCategoryUrl().isBlank()) {
            String categoryUrl = SlugConvert.convert(productDTO.getCategoryUrl());
            Category category = this.categoryRepository.findByUrlKey(categoryUrl).orElseThrow(
                    () -> new IdInvalidException("Category not found with url: " + categoryUrl)
            );
            existingProduct.setCategory(category);
        }

        // Options (nếu có)
        if (productDTO.getOptions() != null) {
            List<ProductOption> optionEntities = productDTO.getOptions().stream()
                    .map(optDTO -> {
                        ProductOption option = new ProductOption();
                        option.setId(optDTO.getId()); // Nếu update option cũ
                        option.setName(optDTO.getName());
                        option.setValue(optDTO.getValue());
                        option.setAddPrice(optDTO.getAddPrice());
                        option.setSubPrice(optDTO.getSubPrice());
                        option.setProduct(existingProduct);
                        return option;
                    })
                    .collect(Collectors.toList());
            existingProduct.setOptions(optionEntities);
        }

        // Specifications (nếu có)
        if (productDTO.getSpecifications() != null) {
            Set<ProductSpecification> specEntities = productDTO.getSpecifications().stream()
                    .map(specDTO -> {
                        ProductSpecification spec = new ProductSpecification();
                        spec.setId(specDTO.getId());
                        spec.setName(specDTO.getName());
                        spec.setValue(specDTO.getValue());
                        spec.setProduct(existingProduct);
                        return spec;
                    })
                    .collect(Collectors.toSet());
            existingProduct.setSpecifications(specEntities);
        }

        Product updated = productRepository.save(existingProduct);
        return productMapper.mapToResponse(updated);
    }

    @Override
    public Map<Long, ProductItemBriefDTO> getProductItemBriefByOptionIds(List<Long> optionIds) {
        if (optionIds == null || optionIds.isEmpty()) return Collections.emptyMap();

        // Lọc null + distinct để tránh query thừa
        List<Long> ids = optionIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (ids.isEmpty()) return Collections.emptyMap();

        // Lấy options kèm product (tránh N+1)
        List<ProductOption> options = this.productOptionRepository.findWithProductByIdIn(ids);
        if (options.isEmpty()) return Collections.emptyMap();

        // Map tạm: optionId -> DTO
        Map<Long, ProductItemBriefDTO> temp = options.stream()
                .collect(Collectors.toMap(
                        ProductOption::getId,
                        this::toItemBrief,
                        (a, b) -> a
                ));

        // Giữ đúng thứ tự theo input và bỏ những id không tồn tại
        Map<Long, ProductItemBriefDTO> out = new LinkedHashMap<>();
        for (Long id : optionIds) {
            ProductItemBriefDTO dto = temp.get(id);
            if (dto != null) out.put(id, dto);
        }
        return out;
    }

    private ProductItemBriefDTO toItemBrief(ProductOption o) {
        Product p = o.getProduct();

        double base = safe(p.getPrice());
        double add  = safe(o.getAddPrice());
        double sub  = safe(o.getSubPrice());
        double disc = safe(p.getDiscountRate()); // 10 = 10%

        double finalPrice = base + add - sub;
        if (finalPrice < 0) finalPrice = 0;

        double discounted = finalPrice * (1 - disc / 100.0);
        if (discounted < 0) discounted = 0;

        ProductItemBriefDTO dto = new ProductItemBriefDTO();
        dto.setProductId(p.getId());
        dto.setOptionId(o.getId());
        dto.setName(p.getName());
        dto.setImage(p.getThumbnailUrl());
        dto.setProductSlug(p.getProductSlug());
        dto.setOptionLabel(((o.getName() == null) ? "" : o.getName()) +
                ((o.getValue() == null) ? "" : " " + o.getValue()));
        dto.setAvailable(true); // nối inventory sau nếu cần

        dto.setBasePrice(base);
        dto.setAddPrice(add);
        dto.setSubPrice(sub);
        dto.setDiscountPercent(disc);
        dto.setFinalPrice(finalPrice);
        dto.setDiscountedFinalPrice(discounted);

        return dto;
    }

    private double safe(Double v) {
        return v == null ? 0.0 : v;
    }


    @Override
    public void deleteProduct(Long productId) {
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product","productId", productId));
        productRepository.delete(existingProduct);
    }

    @Override
    public ProductResponseDTO updateStock(Long productId, int quantity) {
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() ->  new ResourceNotFoundException("Product","productId", productId));

        existingProduct.setQuantityStock(quantity);
        Product saved = productRepository.save(existingProduct);
        return productMapper.mapToResponse(saved);
    }

}
