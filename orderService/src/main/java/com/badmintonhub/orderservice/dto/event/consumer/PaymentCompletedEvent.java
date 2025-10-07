package com.badmintonhub.orderservice.dto.event.consumer;

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
public class PaymentCompletedEvent {
    /** Idempotency cho consumer */
    @NotBlank
    private String eventId;          // UUID mỗi lần publish

    @NotBlank
    private String orderId;

    /** (tuỳ) Ai thanh toán */
    @NotBlank
    private String userId;

    /** Tham chiếu payment trong PaymentService */
    @NotBlank
    private String paymentId;

    /** Thông tin số tiền đã charge */
    @NotNull
    private BigDecimal amount;       // ví dụ: 1200000.00

    @NotBlank
    private String currency;         // "VND", "USD", ...

    /** Provider/method để trace (tuỳ chọn) */
    private String provider;         // "PayPal", "Stripe", ...
    private String method;           // "CARD", "WALLET", "BANK_TRANSFER", ...

    /** Thời điểm hoàn tất */
    @NotNull
    private Instant completedAt;

    /** Factory tiện dụng */
    public static PaymentCompletedEvent of(String orderId,
                                           String userId,
                                           String paymentId,
                                           BigDecimal amount,
                                           String currency,
                                           String provider,
                                           String method) {
        return PaymentCompletedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .orderId(orderId)
                .userId(userId)
                .paymentId(paymentId)
                .amount(amount)
                .currency(currency)
                .provider(provider)
                .method(method)
                .completedAt(Instant.now())
                .build();
    }
}
