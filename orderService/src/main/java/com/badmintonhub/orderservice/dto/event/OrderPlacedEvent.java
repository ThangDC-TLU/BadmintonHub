package com.badmintonhub.orderservice.dto.event;

import com.badmintonhub.orderservice.utils.constant.PaymentMethodEnum;
import com.badmintonhub.orderservice.utils.constant.PaymentStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPlacedEvent {

    // ===== Metadata (an toàn / theo dõi) =====
    private String eventId;              // UUID – idempotency cho consumer
    private String type;                 // "order.placed"
    private String version;              // "v1"
    private Instant timestamp;           // thời điểm phát event

    // ===== Thông tin người nhận email =====
    private Long userId;               // id người dùng (nếu có)
    private String customerEmail;        // email gửi tới (bắt buộc)
    private String customerName;         // tên hiển thị (Mr A)
    private String locale;               // "vi-VN", "en-US" (format ngày/tiền)
    private String timezone;             // "Asia/Ho_Chi_Minh" (render thời gian local)

    // ===== Thông tin đơn hàng để render =====
    private String orderCode;            // mã đơn (hiển thị trong subject)
    private Instant createdAt;           // thời điểm tạo đơn (FE format theo locale/tz)

    private String currency;             // "VND", "USD"
    private BigDecimal subtotal;         // tổng dòng hàng (sum item.unitPrice * quantity)
    private BigDecimal discountTotal;    // tổng giảm giá (nếu có) – có thể null
    private BigDecimal shippingFee;      // phí ship
    private BigDecimal taxTotal;         // thuế (nếu có) – có thể null
    private BigDecimal grandTotal;       // tổng phải trả (đưa ra email)

    private PaymentMethodEnum paymentMethod;  // COD / PAYPAL / ...
    private PaymentStatusEnum paymentStatus;  // UNPAID / PENDING / PAID ...
    private String approvalUrl;               // link PayPal approve (nếu cần)

    private ShippingAddress shippingAddress;  // địa chỉ giao
    private List<Item> items;                 // các dòng hàng tối thiểu để hiển thị

    // ===== Link phục vụ người dùng =====
    private String orderDetailUrl;       // link xem chi tiết đơn (FE)
    private String trackingUrl;          // link theo dõi đơn (nếu có)

    // ===== Tùy chọn khác =====
    private Map<String, Object> meta;    // chèn thêm biến nếu template cần

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ShippingAddress {
        private String name;             // tên người nhận
        private String phone;
        private String oneLine;          // "123 Lê Lợi, P.X, Q.Y, TP.HCM"
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Item {
        private Long productId;
        private Long optionId;
        private String name;
        private String optionLabel;      // "3U/G5", "Size M"...
        private String image;            // URL ảnh (nhỏ)
        private Integer quantity;

        private BigDecimal unitPrice;    // giá hiển thị (sau giảm)
        private BigDecimal lineTotal;    // unitPrice * quantity
    }
}

