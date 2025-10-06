package com.badmintonhub.inventoryservice.service;

import com.badmintonhub.inventoryservice.dto.message.ObjectResponse;
import com.badmintonhub.inventoryservice.dto.response.StockRowResponse;
import com.badmintonhub.inventoryservice.entity.InventoryLevel;
import com.badmintonhub.inventoryservice.repository.InventoryLevelRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryQueryService {
    private final InventoryLevelRepository inventoryLevelRepository;

    public InventoryQueryService(InventoryLevelRepository inventoryLevelRepository) {
        this.inventoryLevelRepository = inventoryLevelRepository;
    }

    public ObjectResponse pageStock(Specification<InventoryLevel> spec,
                                    Pageable pageable) {
        Page<InventoryLevel> page = inventoryLevelRepository.findAll(pageable);

        List<StockRowResponse> rows = page.getContent().stream().map(lv -> StockRowResponse.builder()
                .skuCode(lv.getSku().getSkuCode())
                .name(lv.getSku().getName())
                .productId(lv.getSku().getProductId())
                .barcode(lv.getSku().getBarcode())
                .isActive(lv.getSku().getIsActive())
                .onHand(lv.getOnHand())
                .reserved(lv.getReserved())
                .allocated(lv.getAllocated())
                .updatedAt(lv.getUpdatedAt())
                .build()).collect(Collectors.toList());

        ObjectResponse.Meta meta = new ObjectResponse.Meta();
        meta.setPage(page.getNumber()+1);
        meta.setPageSize(page.getSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());

        ObjectResponse body = new ObjectResponse();
        body.setMeta(meta);
        body.setResult(rows);
        return body;
    }
}
