package com.badmintonhub.productservice.service;

import com.badmintonhub.productservice.dto.message.ObjectResponse;
import com.badmintonhub.productservice.dto.model.CategoryDTO;
import com.badmintonhub.productservice.dto.response.CategoryResponseDTO;
import com.badmintonhub.productservice.entity.Category;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

public interface CategoryService {
    CategoryResponseDTO creatCategory(CategoryDTO categoryDTO);
    ObjectResponse getAllCategories(Specification<Category> specification, Pageable pageable);

    CategoryResponseDTO getCategoryById(Long id);

    CategoryResponseDTO updateCategory(Long id, CategoryDTO categoryDTO);

    void deleteCategory(Long id);

    List<CategoryResponseDTO> getCategoriesByParentId(Long parentId);
}
