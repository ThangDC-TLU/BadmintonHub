package com.badmintonhub.inventoryservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "inventory_levels",
        uniqueConstraints = @UniqueConstraint(name = "uk_sku", columnNames = {"sku_id"}))
public class InventoryLevel {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Mỗi SKU chỉ có 1 dòng số dư vì chỉ 1 kho */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sku_id", nullable = false, unique = true)
    private Sku sku;

    /** Số đang có vật lý trong kho */
    @Column(nullable = false)
    private int onHand;

    /** Đã giữ chỗ cho đơn (chưa trừ onHand) */
    @Column(nullable = false)
    private int reserved;

    /** Đã cam kết sau thanh toán, chờ xuất — giúp tách 2 bước allocate và ship */
    @Column(nullable = false)
    private int allocated;

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate @PrePersist
    void touch() { this.updatedAt = Instant.now(); }

    @Transient
    public int getAvailable() { return onHand - reserved - allocated; }
}
