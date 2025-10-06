package com.badmintonhub.inventoryservice.service;

import com.badmintonhub.inventoryservice.entity.InventoryLevel;
import com.badmintonhub.inventoryservice.entity.Sku;
import com.badmintonhub.inventoryservice.exception.BadRequestException;
import com.badmintonhub.inventoryservice.repository.InventoryLevelRepository;
import com.badmintonhub.inventoryservice.repository.SkuRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SkuService {
    private final SkuRepository skuRepository;
    private final InventoryLevelRepository inventoryLevelRepository;
    public SkuService(SkuRepository skuRepository, InventoryLevelRepository inventoryLevelRepository) {
        this.skuRepository = skuRepository;
        this.inventoryLevelRepository = inventoryLevelRepository;
    }


    @Transactional
    public Sku createOrUpdate(Sku entity) {
        if (entity == null) {
            throw new BadRequestException("Body is required");
        }
        if (entity.getSkuCode() == null || entity.getSkuCode().isBlank()) {
            throw new BadRequestException("skuCode is required");
        }
        String code = entity.getSkuCode().trim();

        // Validate số liệu nếu được truyền
        if (entity.getWeightGram() != null && entity.getWeightGram() < 0) {
            throw new BadRequestException("weightGram must be >= 0");
        }
        if (entity.getWidthMm() != null && entity.getWidthMm() < 0) {
            throw new BadRequestException("widthMm must be >= 0");
        }
        if (entity.getHeightMm() != null && entity.getHeightMm() < 0) {
            throw new BadRequestException("heightMm must be >= 0");
        }
        if (entity.getLengthMm() != null && entity.getLengthMm() < 0) {
            throw new BadRequestException("lengthMm must be >= 0");
        }

        Sku target = skuRepository.findBySkuCode(code).orElse(null);
        if (target == null) {
            target = new Sku();
            target.setSkuCode(code);
            // isActive mặc định true theo entity; giữ nguyên nếu không truyền
        }

        // Ghi đè các trường nếu request có giá trị (partial update)
        if (entity.getProductId() != null) {
            target.setProductId(entity.getProductId());
        }
        if (entity.getName() != null) {
            String name = entity.getName().trim();
            if (name.isEmpty()) throw new BadRequestException("name cannot be blank");
            target.setName(name);
        }
        if (entity.getOptionJson() != null) {
            target.setOptionJson(entity.getOptionJson());
        }
        if (entity.getBarcode() != null) {
            String barcode = entity.getBarcode().trim();
            target.setBarcode(barcode.isEmpty() ? null : barcode);
        }
        if (entity.getWeightGram() != null) {
            target.setWeightGram(entity.getWeightGram());
        }
        if (entity.getWidthMm() != null) {
            target.setWidthMm(entity.getWidthMm());
        }
        if (entity.getHeightMm() != null) {
            target.setHeightMm(entity.getHeightMm());
        }
        if (entity.getLengthMm() != null) {
            target.setLengthMm(entity.getLengthMm());
        }
        if (entity.getIsActive() != null) {
            target.setIsActive(entity.getIsActive());
        }

        // Bắt buộc phải có trước khi lưu
        if (target.getName() == null || target.getName().isBlank()) {
            throw new BadRequestException("name is required");
        }
        if (target.getProductId() == null) {
            throw new BadRequestException("productId is required");
        }

        Sku saved = skuRepository.save(target);

        // Đảm bảo có dòng tồn kho cho SKU mới
        inventoryLevelRepository.findBySkuId(saved.getId()).orElseGet(() -> {
            InventoryLevel lv = InventoryLevel.builder()
                    .sku(saved)
                    .onHand(0)
                    .reserved(0)
                    .allocated(0)
                    .build();
            return inventoryLevelRepository.save(lv);
        });

        return saved;
    }

}
