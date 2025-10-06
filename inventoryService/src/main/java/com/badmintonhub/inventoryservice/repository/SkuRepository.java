package com.badmintonhub.inventoryservice.repository;

import com.badmintonhub.inventoryservice.entity.Sku;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SkuRepository extends JpaRepository<Sku,Long>, JpaSpecificationExecutor<Sku> {

    Optional<Sku> findBySkuCode(String code);
}
