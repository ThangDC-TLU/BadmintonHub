// dto/response/ReservationRowResponse.java
package com.badmintonhub.inventoryservice.dto.response;

import com.badmintonhub.inventoryservice.utils.ReservationStatus;
import lombok.*;
import java.time.Instant;

/** Hàng cho trang Reservations Monitor */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReservationRowResponse {
    private Long id;
    private String orderId;
    private String skuCode;
    private String skuName;
    private int quantity;
    private ReservationStatus status;
    private Instant expiresAt;
    private Instant createdAt;
    private Long willExpireInSeconds; // tiện cho countdown
}
