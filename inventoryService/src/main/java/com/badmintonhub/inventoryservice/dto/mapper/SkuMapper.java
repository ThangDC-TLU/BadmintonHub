// dto/mapper/SkuMapper.java
package com.badmintonhub.inventoryservice.dto.mapper;

import com.badmintonhub.inventoryservice.dto.request.SkuUpsertRequest;
import com.badmintonhub.inventoryservice.dto.response.SkuResponse;
import com.badmintonhub.inventoryservice.entity.Sku;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component @RequiredArgsConstructor
public class SkuMapper {
    private final ModelMapper mm;

    public Sku toEntity(SkuUpsertRequest req){
        Sku s = mm.map(req, Sku.class);
        if (s.getSkuCode() != null) s.setSkuCode(s.getSkuCode().trim());
        return s;
    }

    public SkuResponse toResponse(Sku s){
        return mm.map(s, SkuResponse.class);
    }
}
