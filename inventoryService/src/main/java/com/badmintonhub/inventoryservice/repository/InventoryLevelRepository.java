package com.badmintonhub.inventoryservice.repository;

import com.badmintonhub.inventoryservice.entity.InventoryLevel;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryLevelRepository extends JpaRepository<InventoryLevel,Long> {
    Page<InventoryLevel> findAll(Specification<InventoryLevel> finalSpec, Pageable effective);

    Optional<InventoryLevel> findBySkuId(Long id);
}
