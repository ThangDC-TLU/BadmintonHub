package com.badmintonhub.inventoryservice.repository;

import com.badmintonhub.inventoryservice.entity.Reservation;
import com.badmintonhub.inventoryservice.utils.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation,Long> {
    @Query("select r from Reservation r where r.sku.id = :skuId order by r.createdAt desc")
    List<Reservation> findBySkuIdOrderByCreatedAtDesc(@Param("skuId") Long skuId, Pageable pageable);

    Page<Reservation> findAll(Specification<Reservation> spec, Pageable effective);

    List<Reservation> findByStatusAndExpiresAtBefore(ReservationStatus reservationStatus, Instant now);
}
