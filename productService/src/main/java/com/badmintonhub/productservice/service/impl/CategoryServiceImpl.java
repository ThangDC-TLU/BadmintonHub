package com.badmintonhub.productservice.service.impl;

import com.badmintonhub.productservice.dto.mapper.CategoryMapper;
import com.badmintonhub.productservice.dto.message.ObjectResponse;
import com.badmintonhub.productservice.dto.model.CategoryDTO;
import com.badmintonhub.productservice.dto.response.CategoryResponseDTO;
import com.badmintonhub.productservice.entity.Category;
import com.badmintonhub.productservice.exception.ResourceNotFoundException;
import com.badmintonhub.productservice.repository.CategoryRepository;
import com.badmintonhub.productservice.service.CategoryService;
import com.badmintonhub.productservice.utils.format.SlugConvert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {
    private final CategoryMapper  categoryMapper;
    private final CategoryRepository categoryRepository;
    public CategoryServiceImpl(CategoryMapper categoryMapper, CategoryRepository categoryRepository) {
        this.categoryMapper = categoryMapper;
        this.categoryRepository = categoryRepository;
    }
    @Override
    public CategoryResponseDTO creatCategory(CategoryDTO categoryDTO) {
        Category category = this.categoryMapper.mapToEntity(categoryDTO);

        category.setUrlKey(SlugConvert.convert(category.getName()));

        Category categoryRes = this.categoryRepository.save(category);

        return this.categoryMapper.mapToResponse(categoryRes);
    }

    @Override
    public ObjectResponse getAllCategories(Specification<Category> specification, Pageable pageable) {
        Page<Category> categoryPage = this.categoryRepository.findAll(specification, pageable);
        ObjectResponse objectResponse = new ObjectResponse();
        ObjectResponse.Meta meta = new ObjectResponse.Meta();

        meta.setTotal(categoryPage.getTotalElements());
        meta.setPages(categoryPage.getTotalPages());
        meta.setPage(pageable.getPageNumber()+1);
        meta.setPageSize(pageable.getPageSize());

        objectResponse.setMeta(meta);
        List<CategoryResponseDTO> categoryDtos = categoryPage
                .getContent()
                .stream()
                .map(categoryMapper::mapToResponse)
                .collect(Collectors.toList());

        objectResponse.setResult(categoryDtos);


        return objectResponse;
    }

    @Override
    public CategoryResponseDTO getCategoryById(Long id) {
        Category category = this.categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        return this.categoryMapper.mapToResponse(category);
    }

    @Override
    public CategoryResponseDTO updateCategory(Long id, CategoryDTO categoryDTO) {
        Category category = this.categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        category.setName(categoryDTO.getName());
        category.setUrlKey(SlugConvert.convert(categoryDTO.getName()));
        category.setThumbnailUrl(categoryDTO.getThumbnailUrl());
        category.setParentId(categoryDTO.getParentId());

        Category categoryRes = this.categoryRepository.save(category);
        return this.categoryMapper.mapToResponse(categoryRes);
    }

    @Override
    public void deleteCategory(Long id) {
        Category category = this.categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        this.categoryRepository.delete(category);

    }

    @Override
    public List<CategoryResponseDTO> getCategoriesByParentId(Long parentId) {
        List<Category> categoryList = this.categoryRepository.findByParentId(parentId);
        return categoryList.stream()
                .map(categoryMapper::mapToResponse)
                .collect(Collectors.toList());
    }

}
