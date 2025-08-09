package com.badmintonhub.productservice.repository;

import com.badmintonhub.productservice.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category,Long>, JpaSpecificationExecutor<Category> {
    List<Category> findByParentId(Long parentId);

    Category findByUrlKey(String urlKey);
}
