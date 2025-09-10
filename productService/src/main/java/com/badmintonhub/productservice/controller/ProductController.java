package com.badmintonhub.productservice.controller;

import com.badmintonhub.productservice.dto.message.ObjectResponse;
import com.badmintonhub.productservice.dto.model.ProductItemBriefDTO;
import com.badmintonhub.productservice.dto.model.ProductDTO;
import com.badmintonhub.productservice.dto.model.ProductUpdateDTO;
import com.badmintonhub.productservice.dto.response.ProductResponseDTO;
import com.badmintonhub.productservice.entity.Product;
import com.badmintonhub.productservice.exception.IdInvalidException;
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
import java.util.Map;

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

    @GetMapping("/{productId}")
    @ApiMessage("Get product by id")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable("productId") Long productId) {
        return ResponseEntity.ok(this.productService.getProductById(productId));
    }

    @PostMapping
    @ApiMessage("Create a product")
    public ResponseEntity<ProductResponseDTO> createProduct(@Valid @RequestBody ProductDTO productDTO) throws IdInvalidException {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.productService.createProduct(productDTO));
    }

    @PutMapping("/{productId}")
    @ApiMessage("Update a product")
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @PathVariable("productId") Long productId,
            @Valid @RequestBody ProductUpdateDTO productUpdateDTO
    ) throws IdInvalidException {
        return ResponseEntity.ok(this.productService.updateProduct(productId, productUpdateDTO));
    }

    @DeleteMapping("/{productId}")
    @ApiMessage("Delete a product")
    public ResponseEntity<Void> deleteProduct(@PathVariable("productId") Long productId) {
        this.productService.deleteProduct(productId);
        return ResponseEntity.ok(null);
    }

    @PatchMapping("/{productId}/stock/{quantity}")
    @ApiMessage("Update product stock quantity")
    public ResponseEntity<ProductResponseDTO> updateStock(
            @PathVariable("productId") Long productId,
            @PathVariable("quantity") int quantity
    ) {
        return ResponseEntity.ok(this.productService.updateStock(productId, quantity));
    }

    @PostMapping(path = "/product-options")
    public ResponseEntity<Map<Long, ProductItemBriefDTO>> findProductsByProductOptionIds(
            @RequestBody List<Long> optionIds
    ) {
        if (optionIds == null || optionIds.isEmpty()) {
            return ResponseEntity.ok(Map.of());
        }
        Map<Long, ProductItemBriefDTO> result =
                productService.getProductItemBriefByOptionIds(optionIds);
        return ResponseEntity.ok(result);
    }


}
