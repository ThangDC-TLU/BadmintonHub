package com.badmintonhub.productservice.repository;

import com.badmintonhub.productservice.entity.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductOptionRepository extends JpaRepository<ProductOption,Long>, JpaSpecificationExecutor<ProductOption> {
}
