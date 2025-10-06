// dto/response/SkuDetailResponse.java
package com.badmintonhub.inventoryservice.dto.response;

import com.badmintonhub.inventoryservice.utils.ReservationStatus;
import lombok.*;
import java.time.Instant;
import java.util.List;

/** Dữ liệu cho drawer chi tiết SKU: sku + level + vài reservation gần nhất */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SkuDetailResponse {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SkuBasic {
        private String skuCode;
        private Long productId;
        private String name;
        private String optionJson;
        private String barcode;
        private Boolean isActive;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class LevelBasic {
        private int onHand;
        private int reserved;
        private int allocated;
        private int available;
        private Instant updatedAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ReservationBrief {
        private Long id;
        private String orderId;
        private int quantity;
        private ReservationStatus status;
        private Instant expiresAt;
        private Instant createdAt;
    }

    private SkuBasic sku;
    private LevelBasic level;
    private List<ReservationBrief> recentReservations;
}
