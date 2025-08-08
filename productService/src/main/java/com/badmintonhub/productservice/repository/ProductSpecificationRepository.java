package com.badmintonhub.productservice.repository;

import com.badmintonhub.productservice.entity.ProductSpecification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductSpecificationRepository extends JpaRepository<ProductSpecification,Long>, JpaSpecificationExecutor<ProductSpecification> {
}
