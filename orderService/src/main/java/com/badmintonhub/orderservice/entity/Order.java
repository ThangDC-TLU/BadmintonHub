package com.badmintonhub.orderservice.entity;

import com.badmintonhub.orderservice.utils.constant.OrderStatusEnum;
import com.badmintonhub.orderservice.utils.constant.PaymentMethodEnum;
import com.badmintonhub.orderservice.utils.constant.PaymentStatusEnum;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "orders",
        indexes = {
                @Index(name = "idx_orders_user", columnList = "userId"),
                @Index(name = "idx_orders_status", columnList = "orderStatus"),
                @Index(name = "idx_orders_created_at", columnList = "createdAt")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_orders_code", columnNames = "orderCode")
        }
)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Mã đơn công khai */
    private String orderCode;

    private Long userId;

    /** Không snapshot địa chỉ: tham chiếu id*/
    private Long addressId;

    /** Tiền tệ */
    @Builder.Default
    private String currency = "VND";

    /** Tổng tiền (snapshot) – BigDecimal khuyên dùng */
    private BigDecimal subtotal;
    private BigDecimal discountTotal;
    private BigDecimal shippingFee;
    private BigDecimal taxTotal;
    private BigDecimal grandTotal;

    /** Trạng thái/Thanh toán */
    @Enumerated(EnumType.STRING)
    private OrderStatusEnum orderStatus;

    @Enumerated(EnumType.STRING)
    private PaymentMethodEnum paymentMethod;

    @Enumerated(EnumType.STRING)
    private PaymentStatusEnum paymentStatus;

    /** id giao dịch từ PSP (vd: PayPal orderId) */
    private String paymentId;

    private String note;

    /** Quan hệ items (snapshot từng dòng) */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    /** Audit */
    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    private Instant expiresAt;

    @Version // bật optimistic locking
    private Long version;
}

