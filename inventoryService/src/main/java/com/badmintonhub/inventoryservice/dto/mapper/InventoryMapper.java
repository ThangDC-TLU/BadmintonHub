// dto/mapper/InventoryMapper.java
package com.badmintonhub.inventoryservice.dto.mapper;

import com.badmintonhub.inventoryservice.dto.response.InventoryLevelResponse;
import com.badmintonhub.inventoryservice.entity.InventoryLevel;
import org.springframework.stereotype.Component;

@Component
public class InventoryMapper {
    public InventoryLevelResponse toLevelResponse(String skuCode, InventoryLevel lv){
        return InventoryLevelResponse.builder()
                .skuCode(skuCode)
                .onHand(lv.getOnHand())
                .reserved(lv.getReserved())
                .allocated(lv.getAllocated())
                .available(lv.getOnHand() - lv.getReserved() - lv.getAllocated())
                .build();
    }
}
