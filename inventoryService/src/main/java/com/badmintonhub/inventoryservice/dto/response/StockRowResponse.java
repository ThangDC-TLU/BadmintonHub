// dto/response/StockRowResponse.java
package com.badmintonhub.inventoryservice.dto.response;

import lombok.*;
import java.time.Instant;

/** Hàng gộp cho trang Inventory (Catalog & Stock): SKU + tồn */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StockRowResponse {
    private String skuCode;
    private String name;
    private Long productId;
    private String barcode;
    private Boolean isActive;

    private int onHand;
    private int reserved;
    private int allocated;

    private Instant updatedAt;
}
