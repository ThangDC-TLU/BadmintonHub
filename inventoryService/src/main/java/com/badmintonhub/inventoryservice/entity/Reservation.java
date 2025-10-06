package com.badmintonhub.inventoryservice.entity;

import com.badmintonhub.inventoryservice.utils.ReservationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "reservations")
public class Reservation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** orderId từ OrderService (tham chiếu mềm) */
    @Column(nullable = false, length = 100)
    private String orderId;

    /** chống double-submit khi retry */
    @Column(unique = true, length = 100)
    private String idempotencyKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sku_id", nullable = false)
    private Sku sku;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status = ReservationStatus.ACTIVE;

    private Instant expiresAt;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();
}
