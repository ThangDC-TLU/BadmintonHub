package com.badmintonhub.productservice.controller;

import com.badmintonhub.productservice.dto.message.ObjectResponse;
import com.badmintonhub.productservice.dto.model.CategoryDTO;
import com.badmintonhub.productservice.dto.response.CategoryResponseDTO;
import com.badmintonhub.productservice.entity.Category;
import com.badmintonhub.productservice.service.CategoryService;
import com.badmintonhub.productservice.utils.anotation.ApiMessage;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    @ApiMessage("Get all categories")
    public ResponseEntity<ObjectResponse> getAllCategories(
            @Filter Specification<Category> spec, Pageable pageable
    ) {
        return ResponseEntity.ok(this.categoryService.getAllCategories(spec, pageable));
    }

    @PostMapping
    @ApiMessage("Create a category")
    public ResponseEntity<CategoryResponseDTO> createCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.categoryService.creatCategory(categoryDTO));
    }

    @GetMapping("/{categoryId}")
    @ApiMessage("Get category by ID")
    public ResponseEntity<CategoryResponseDTO> getCategoryById(@PathVariable("categoryId") Long id) {
        CategoryResponseDTO category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }

    @PutMapping("/{categoryId}")
    @ApiMessage("Update category")
    public ResponseEntity<CategoryResponseDTO> updateCategory(
            @PathVariable("categoryId") Long id,
            @Valid @RequestBody CategoryDTO categoryDTO
    ) {
        CategoryResponseDTO updatedCategory = categoryService.updateCategory(id, categoryDTO);
        return ResponseEntity.ok(updatedCategory);
    }

    @DeleteMapping("/{categoryId}")
    @ApiMessage("Delete category")
    public ResponseEntity<Void> deleteCategory(@PathVariable("categoryId") Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/parent/{parentId}")
    @ApiMessage("Get categories by parent ID")
    public ResponseEntity<List<CategoryResponseDTO>> getCategoriesByParentId(@PathVariable("parentId") Long parentId) {
        List<CategoryResponseDTO> categories = categoryService.getCategoriesByParentId(parentId);
        return ResponseEntity.ok(categories);
    }
}
