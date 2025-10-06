package com.badmintonhub.inventoryservice.controller;

import com.badmintonhub.inventoryservice.dto.mapper.InventoryMapper;
import com.badmintonhub.inventoryservice.dto.mapper.SkuMapper;
import com.badmintonhub.inventoryservice.dto.message.ObjectResponse;
import com.badmintonhub.inventoryservice.dto.request.*;
import com.badmintonhub.inventoryservice.dto.response.*;
import com.badmintonhub.inventoryservice.entity.InventoryLevel;
import com.badmintonhub.inventoryservice.entity.Reservation;
import com.badmintonhub.inventoryservice.entity.Sku;
import com.badmintonhub.inventoryservice.service.*;
import com.badmintonhub.inventoryservice.utils.ApiMessage;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final SkuService skuService;
    private final InventoryService inventoryService;
    private final InventoryQueryService inventoryQueryService;
    private final ReservationService reservationService;
    private final SkuMapper skuMapper;
    private final InventoryMapper inventoryMapper;

    public InventoryController(SkuService skuService,
                               InventoryService inventoryService,
                               InventoryQueryService inventoryQueryService,
                               ReservationService reservationService,
                               SkuMapper skuMapper,
                               InventoryMapper inventoryMapper) {
        this.skuService = skuService;
        this.inventoryService = inventoryService;
        this.inventoryQueryService = inventoryQueryService;
        this.reservationService = reservationService;
        this.skuMapper = skuMapper;
        this.inventoryMapper = inventoryMapper;
    }

    // ===== INVENTORY (Catalog & Stock) =====

    /** List gộp SKU + tồn cho bảng chính (paged + filter Turkraft). Có optional availableLt để highlight. */
    @GetMapping("/stock")
    @ApiMessage("List stock (SKU + Level)")
    public ResponseEntity<ObjectResponse> getAllStock(
            @Filter Specification<InventoryLevel> spec,
            @PageableDefault(size = 20, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable
    ){
        return ResponseEntity.ok(inventoryQueryService.pageStock(spec, pageable));
    }

    /** Drawer detail: trả sku + level + vài reservation gần nhất */
    @GetMapping("/stock/{skuCode}/detail")
    @ApiMessage("Get stock detail by skuCode")
    public ResponseEntity<SkuDetailResponse> getStockDetail(
            @PathVariable String skuCode,
            @RequestParam(defaultValue = "10") int recentLimit
    ){
        return ResponseEntity.ok(inventoryService.getSkuDetail(skuCode, recentLimit));
    }

    /** Set onHand tuyệt đối */
    @PostMapping("/levels/{sku}/set")
    public ResponseEntity<InventoryLevelResponse> setLevel(
            @PathVariable String sku,
            @Valid @RequestBody InventorySetLevelRequest req){
        return ResponseEntity.ok(inventoryService.setOnHand(sku, req));
    }


    /** Upsert 1 SKU */
    @PostMapping("/skus")
    public ResponseEntity<SkuResponse> upsertSku(@Valid @RequestBody SkuUpsertRequest req){
        Sku saved = skuService.createOrUpdate(skuMapper.toEntity(req));
        return ResponseEntity.ok(skuMapper.toResponse(saved));
    }

    /** Upsert nhiều SKU */
    @PostMapping("/skus/batch")
    public ResponseEntity<List<SkuResponse>> upsertSkuBatch(@Valid @RequestBody SkuBatchUpsertRequest req){
        List<SkuResponse> list = req.getItems().stream()
                .map(skuMapper::toEntity)
                .map(skuService::createOrUpdate)
                .map(skuMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }


    // ===== RESERVATIONS MONITOR =====

    /** List reservations (paged + filter Turkraft). Đủ cột cho bảng monitor. */
    @GetMapping("/reservations")
    @ApiMessage("List reservations")
    public ResponseEntity<ObjectResponse> getAllReservations(
            @Filter Specification<Reservation> spec,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ){
        return ResponseEntity.ok(reservationService.pageReservations(spec, pageable));
    }

    /** Nút bảo trì: release tất cả reservation ACTIVE đã hết TTL */
    @PostMapping("/reservations/release-expired")
    public ResponseEntity<MaintenanceReleaseResponse> releaseExpired(){
        return ResponseEntity.ok(reservationService.releaseExpired());
    }

    /** Release thủ công 1 reservation ACTIVE */
    @PostMapping("/reservations/{id}/release")
    public ResponseEntity<Void> releaseOne(@PathVariable Long id){
        reservationService.releaseOne(id);
        return ResponseEntity.noContent().build();
    }
}
