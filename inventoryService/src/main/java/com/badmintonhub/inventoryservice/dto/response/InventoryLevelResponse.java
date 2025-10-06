// dto/response/InventoryLevelResponse.java
package com.badmintonhub.inventoryservice.dto.response;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InventoryLevelResponse {
    private String skuCode;
    private int onHand;
    private int reserved;
    private int allocated;
    private int available;
}
