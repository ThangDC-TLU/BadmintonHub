package com.badmintonhub.productservice.repository;

import com.badmintonhub.productservice.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    boolean existsByProductSlug(String slug);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Product p set p.ratingAverage=:avg, p.reviewCount=:cnt, p.ratingUpdatedAt=:ts where p.id=:id")
    int updateRatingAndCount(@Param("id") Long productId,
                             @Param("avg") double ratingAverage,
                             @Param("cnt") int reviewCount,
                             @Param("ts") Instant occurredAt);

    @Query("select p.ratingUpdatedAt from Product p where p.id=:id")
    Instant getRatingUpdatedAt(@Param("id") Long productId);
}
