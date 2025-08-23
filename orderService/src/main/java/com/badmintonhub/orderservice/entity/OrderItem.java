package com.badmintonhub.orderservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Liên kết tới Order (bắt buộc) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Order order;

    /** Nhận diện sản phẩm/biến thể */
    private Long productId;
    private Long optionId;

    /** Snapshot hiển thị từ ProductItemBriefDTO */
    private String nameSnapshot;          // = dto.getName()
    private String imageSnapshot;         // = dto.getImage()
    private String optionLabelSnapshot;   // = dto.getOptionLabel()

    /** Số lượng */
    @Builder.Default
    private Integer quantity = 0;

    /** ĐƠN GIÁ (snapshot) */
    @Builder.Default
    private BigDecimal unitBasePrice = BigDecimal.ZERO;        // basePrice
    @Builder.Default
    private BigDecimal unitAddPrice = BigDecimal.ZERO;         // addPrice
    @Builder.Default
    private BigDecimal unitSubPrice = BigDecimal.ZERO;         // subPrice
    @Builder.Default
    private BigDecimal unitFinalPrice = BigDecimal.ZERO;       // finalPrice
    @Builder.Default
    private BigDecimal unitDiscountPercent = BigDecimal.ZERO;  // discountPercent 0..100
    @Builder.Default
    private BigDecimal unitDiscountedPrice = BigDecimal.ZERO;  // discountedFinalPrice

    /** THÀNH TIỀN (snapshot) */
    @Builder.Default
    private BigDecimal lineFinalPrice = BigDecimal.ZERO;       // finalPrice * quantity
    @Builder.Default
    private BigDecimal lineDiscountedPrice = BigDecimal.ZERO;  // discountedFinalPrice * quantity

    /** Audit */
    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}

