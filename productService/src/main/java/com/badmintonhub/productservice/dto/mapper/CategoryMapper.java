package com.badmintonhub.productservice.dto.mapper;


import com.badmintonhub.productservice.dto.response.CategoryResponseDTO;
import com.badmintonhub.productservice.dto.model.CategoryDTO;
import com.badmintonhub.productservice.entity.Category;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {
    private final ModelMapper modelMapper;

    public CategoryMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public CategoryDTO mapToDTO(Category category){
        CategoryDTO categoryDTO = modelMapper.map(category, CategoryDTO.class);
        return categoryDTO;
    }
    public Category mapToEntity(CategoryDTO categoryDTO){
        Category category = modelMapper.map(categoryDTO, Category.class);
        return category;
    }

    public CategoryResponseDTO mapToResponse(Category category) {
        CategoryResponseDTO dto = new CategoryResponseDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setUrlKey(category.getUrlKey());
        dto.setThumbnailUrl(category.getThumbnailUrl());

        // Tránh lỗi đệ quy
        if (category.getParent() != null) {
            dto.setParentId(category.getParent().getId());
        }

        return dto;
    }

}
