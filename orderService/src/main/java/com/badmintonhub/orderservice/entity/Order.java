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
@Entity @Table(name = "orders")
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderCode;        // unique
    private Long userId;
    private Long addressId;          // tham chiếu nguồn (không version)

    // --- snapshot địa chỉ (flatten cho đơn giản) ---
    private String shipName;
    private String shipPhone;
    private String shipCompany;
    private String shipProvince;
    private String shipDistrict;
    private String shipWard;
    private String shipAddress;
    private String shipAddressType;

    // --- tiền tệ & tổng tiền ---
    @Builder.Default
    private String currency = "VND";
    private BigDecimal subtotal;
    private BigDecimal discountTotal;
    private BigDecimal shippingFee;
    private BigDecimal taxTotal;
    private BigDecimal grandTotal;

    // --- trạng thái & thanh toán ---
    @Enumerated(EnumType.STRING)
    private OrderStatusEnum orderStatus;
    @Enumerated(EnumType.STRING)
    private PaymentMethodEnum paymentMethod;
    @Enumerated(EnumType.STRING)
    private PaymentStatusEnum paymentStatus;

    private String paymentId;    // ví dụ PayPal orderId

    private String note;

    // items (nên có, snapshot từng dòng)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    private Instant expiresAt;

    @Version // bật optimistic locking
    private Long version;
}


