package com.badmintonhub.orderservice.dto.event;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent {
    @NotBlank
    private String eventId;   // UUID cho event (idempotency ở consumer)

    @NotBlank
    private String orderId;   // UUID đơn hàng

    @NotBlank
    private Long userId;

    @NotNull
    private BigDecimal totalAmount; // Đồng bộ với Order (BigDecimal)

    @NotNull
    private List<OrderItemPayload> items;

    @NotNull
    private Instant occurredAt;

    /** Factory tiện dụng để tạo event chuẩn */
    public static OrderCreatedEvent of(String orderId,
                                       Long userId,
                                       BigDecimal totalAmount,
                                       List<OrderItemPayload> items) {
        return OrderCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .orderId(orderId)
                .userId(userId)
                .totalAmount(totalAmount)
                .items(items)
                .occurredAt(Instant.now())
                .build();
    }
}