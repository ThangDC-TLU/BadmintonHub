package com.badmintonhub.inventoryservice.service;

import com.badmintonhub.inventoryservice.dto.message.ObjectResponse;
import com.badmintonhub.inventoryservice.dto.response.MaintenanceReleaseResponse;
import com.badmintonhub.inventoryservice.dto.response.ReservationRowResponse;
import com.badmintonhub.inventoryservice.entity.InventoryLevel;
import com.badmintonhub.inventoryservice.entity.Reservation;
import com.badmintonhub.inventoryservice.exception.BadRequestException;
import com.badmintonhub.inventoryservice.exception.NotFoundException;
import com.badmintonhub.inventoryservice.repository.InventoryLevelRepository;
import com.badmintonhub.inventoryservice.repository.ReservationRepository;
import com.badmintonhub.inventoryservice.utils.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final InventoryLevelRepository inventoryLevelRepository;

    public ReservationService(ReservationRepository reservationRepository, InventoryLevelRepository inventoryLevelRepository) {
        this.reservationRepository = reservationRepository;
        this.inventoryLevelRepository = inventoryLevelRepository;
    }

    @Transactional(readOnly = true)
    public ObjectResponse pageReservations(Specification<Reservation> spec, Pageable pageable) {
        Pageable effective = pageable;
        if (effective == null) {
            effective = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
        } else if (effective.getSort().isUnsorted()) {
            effective = PageRequest.of(effective.getPageNumber(), effective.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "createdAt"));
        }

        Page<Reservation> page = (spec == null)
                ? reservationRepository.findAll(effective)
                : reservationRepository.findAll(spec, effective);

        List<ReservationRowResponse> rows = page.getContent().stream().map(r -> {
            Long willExpire = null;
            if (r.getStatus() == ReservationStatus.ACTIVE && r.getExpiresAt() != null) {
                long sec = Instant.now().until(r.getExpiresAt(), ChronoUnit.SECONDS);
                willExpire = Math.max(sec, 0L);
            }
            return ReservationRowResponse.builder()
                    .id(r.getId())
                    .orderId(r.getOrderId())
                    .skuCode(r.getSku().getSkuCode())
                    .skuName(r.getSku().getName())
                    .quantity(r.getQuantity())
                    .status(r.getStatus())
                    .expiresAt(r.getExpiresAt())
                    .createdAt(r.getCreatedAt())
                    .willExpireInSeconds(willExpire)
                    .build();
        }).collect(Collectors.toList());

        ObjectResponse.Meta meta = new ObjectResponse.Meta();
        meta.setPage(page.getNumber());
        meta.setPageSize(page.getSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());

        ObjectResponse body = new ObjectResponse();
        body.setMeta(meta);
        body.setResult(rows);
        return body;
    }

    @Transactional
    public MaintenanceReleaseResponse releaseExpired() {
        List<Reservation> list = reservationRepository.findByStatusAndExpiresAtBefore(
                ReservationStatus.ACTIVE, Instant.now());

        int released = 0;
        for (Reservation r : list) {
            InventoryLevel lv = inventoryLevelRepository.findBySkuId(r.getSku().getId())
                    .orElseThrow(() -> new NotFoundException("Level not found"));
            if (lv.getReserved() < r.getQuantity()) {
                throw new BadRequestException("Reserved inconsistency for sku " + r.getSku().getSkuCode());
            }
            lv.setReserved(lv.getReserved() - r.getQuantity());
            inventoryLevelRepository.save(lv);

            r.setStatus(ReservationStatus.EXPIRED);
            reservationRepository.save(r);
            released++;
        }
        return MaintenanceReleaseResponse.builder().released(released).build();
    }

    @Transactional
    public void releaseOne(Long id) {
        if (id == null) {
            throw new BadRequestException("reservation id is required");
        }
        Reservation r = reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Reservation not found: " + id));
        if (r.getStatus() != ReservationStatus.ACTIVE) {
            throw new BadRequestException("Only ACTIVE reservation can be released");
        }

        InventoryLevel lv = inventoryLevelRepository.findBySkuId(r.getSku().getId())
                .orElseThrow(() -> new NotFoundException("Level not found"));
        if (lv.getReserved() < r.getQuantity()) {
            throw new BadRequestException("Reserved inconsistency for sku " + r.getSku().getSkuCode());
        }

        lv.setReserved(lv.getReserved() - r.getQuantity());
        inventoryLevelRepository.save(lv);

        r.setStatus(ReservationStatus.RELEASED);
        reservationRepository.save(r);
    }
}
