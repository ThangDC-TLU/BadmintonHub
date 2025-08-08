package com.badmintonhub.productservice.dto.mapper;

import com.badmintonhub.productservice.dto.model.ProductOptionDTO;
import com.badmintonhub.productservice.entity.ProductOption;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class ProductOptionMapper {
    private final ModelMapper modelMapper;
    public ProductOptionMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public ProductOptionDTO mapToDTO(ProductOption productOption){
        ProductOptionDTO productOptionDTO = modelMapper.map(productOption, ProductOptionDTO.class);
        return productOptionDTO;
    }
    public ProductOption mapToEntity(ProductOptionDTO productOptionDTO){
        ProductOption productOption = modelMapper.map(productOptionDTO, ProductOption.class);
        return productOption;
    }
}
