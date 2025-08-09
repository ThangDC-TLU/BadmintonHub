package com.badmintonhub.productservice.dto.mapper;


import com.badmintonhub.productservice.dto.response.ProductResponseDTO;
import com.badmintonhub.productservice.dto.model.ProductDTO;
import com.badmintonhub.productservice.entity.Product;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {
    private final ModelMapper mapper;

    public ProductMapper(ModelMapper mapper) {
        this.mapper = mapper;
    }

    public Product mapToEntity(ProductDTO productDTO){
        Product product = mapper.map(productDTO, Product.class);
        return product;
    }

    public ProductDTO mapToDTO(Product product){
        ProductDTO productDTO = mapper.map(product, ProductDTO.class);
        return productDTO;
    }

    public ProductResponseDTO mapToResponse(Product product){
        ProductResponseDTO productRes = mapper.map(product, ProductResponseDTO.class);
        return productRes;
    }

}
