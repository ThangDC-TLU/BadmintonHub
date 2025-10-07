package com.badmintonhub.orderservice.dto.event;

import lombok.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCompletedEvent {
    /** Dùng cho idempotency ở consumer */
    @NotBlank
    private String eventId;     // UUID mỗi lần publish

    /** Tham chiếu đơn */
    @NotBlank
    private String orderId;

    /** Ai mua */
    @NotBlank
    private String userId;

    /** Tổng tiền đã chốt (snapshot) */
    @NotNull
    private BigDecimal totalAmount;

    /** Mốc thời gian hoàn tất */
    @NotNull
    private Instant completedAt;

    /** (Optional) Tham chiếu payment để trace */
    private String paymentId;

    /** Tạo event chuẩn */
    public static OrderCompletedEvent of(String orderId,
                                         String userId,
                                         BigDecimal totalAmount,
                                         String paymentId) {
        return OrderCompletedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .orderId(orderId)
                .userId(userId)
                .totalAmount(totalAmount)
                .completedAt(Instant.now())
                .paymentId(paymentId)
                .build();
    }
}
