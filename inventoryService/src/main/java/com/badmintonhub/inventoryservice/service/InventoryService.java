package com.badmintonhub.inventoryservice.service;

import com.badmintonhub.inventoryservice.dto.request.InventorySetLevelRequest;
import com.badmintonhub.inventoryservice.dto.response.InventoryLevelResponse;
import com.badmintonhub.inventoryservice.dto.response.SkuDetailResponse;
import com.badmintonhub.inventoryservice.entity.InventoryLevel;
import com.badmintonhub.inventoryservice.entity.Reservation;
import com.badmintonhub.inventoryservice.entity.Sku;
import com.badmintonhub.inventoryservice.exception.BadRequestException;
import com.badmintonhub.inventoryservice.exception.NotFoundException;
import com.badmintonhub.inventoryservice.repository.InventoryLevelRepository;
import com.badmintonhub.inventoryservice.repository.ReservationRepository;
import com.badmintonhub.inventoryservice.repository.SkuRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryService {
    private final SkuRepository skuRepository;
    private final InventoryLevelRepository inventoryLevelRepository;
    private final ReservationRepository reservationRepository;

    public InventoryService(SkuRepository skuRepository, InventoryLevelRepository inventoryLevelRepository, ReservationRepository reservationRepository) {
        this.skuRepository = skuRepository;
        this.inventoryLevelRepository = inventoryLevelRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional(readOnly = true)
    public SkuDetailResponse getSkuDetail(String skuCode, int recentLimit) {
        if (skuCode == null || skuCode.isBlank()) {
            throw new BadRequestException("skuCode is required");
        }
        String code = skuCode.trim();

        Sku sku = skuRepository.findBySkuCode(code)
                .orElseThrow(() -> new NotFoundException("SKU not found: " + code));

        InventoryLevel lv = inventoryLevelRepository.findBySkuId(sku.getId())
                .orElseThrow(() -> new NotFoundException("Level not found for sku: " + code));

        SkuDetailResponse.SkuBasic skuBasic = SkuDetailResponse.SkuBasic.builder()
                .skuCode(sku.getSkuCode())
                .productId(sku.getProductId())
                .name(sku.getName())
                .optionJson(sku.getOptionJson())
                .barcode(sku.getBarcode())
                .isActive(sku.getIsActive())
                .build();

        int available = lv.getOnHand() - lv.getReserved() - lv.getAllocated();
        SkuDetailResponse.LevelBasic levelBasic = SkuDetailResponse.LevelBasic.builder()
                .onHand(lv.getOnHand())
                .reserved(lv.getReserved())
                .allocated(lv.getAllocated())
                .available(available)
                .updatedAt(lv.getUpdatedAt())
                .build();

        int limit = Math.max(recentLimit, 0);
        List<Reservation> recent = (limit == 0)
                ? java.util.Collections.emptyList()
                : reservationRepository.findBySkuIdOrderByCreatedAtDesc(sku.getId(), PageRequest.of(0, limit));

        List<SkuDetailResponse.ReservationBrief> recentReservations = recent.stream()
                .map(r -> SkuDetailResponse.ReservationBrief.builder()
                        .id(r.getId())
                        .orderId(r.getOrderId())
                        .quantity(r.getQuantity())
                        .status(r.getStatus())
                        .expiresAt(r.getExpiresAt())
                        .createdAt(r.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return SkuDetailResponse.builder()
                .sku(skuBasic)
                .level(levelBasic)
                .recentReservations(recentReservations)
                .build();
    }

    public InventoryLevelResponse setOnHand(String sku, @Valid InventorySetLevelRequest req) {
        if (sku == null || sku.isBlank()) {
            throw new BadRequestException("skuCode is required");
        }
        if (req == null || req.getQuantity() == null) {
            throw new BadRequestException("quantity is required");
        }
        if (req.getQuantity() < 0) {
            throw new BadRequestException("quantity must be >= 0");
        }
        String skuCode = sku.trim();
        Sku s = this.skuRepository.findBySkuCode(skuCode)
                .orElseThrow(() -> new NotFoundException("SKU not found: " + sku));

        InventoryLevel inventoryLevel = this.inventoryLevelRepository.findBySkuId(s.getId())
                .orElseThrow(() -> new NotFoundException("Level not found for sku: " + skuCode));

        int minAllowed = inventoryLevel.getAllocated() + inventoryLevel.getReserved();
        if(req.getQuantity() < minAllowed) {
            throw new BadRequestException("quantity must be >= " + minAllowed);
        }

        inventoryLevel.setOnHand(req.getQuantity());
        inventoryLevelRepository.save(inventoryLevel);

        int available = inventoryLevel.getOnHand() - inventoryLevel.getReserved() - inventoryLevel.getAllocated();
        return InventoryLevelResponse.builder()
                .skuCode(s.getSkuCode())
                .onHand(inventoryLevel.getOnHand())
                .reserved(inventoryLevel.getReserved())
                .allocated(inventoryLevel.getAllocated())
                .available(available)
                .build();
    }


}
