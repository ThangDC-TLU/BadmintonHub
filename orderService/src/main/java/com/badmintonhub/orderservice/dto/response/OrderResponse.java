package com.badmintonhub.orderservice.dto.response;

import com.badmintonhub.orderservice.utils.constant.PaymentMethodEnum;
import com.badmintonhub.orderservice.utils.constant.PaymentStatusEnum;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderResponse {

    private String orderCode;

    private com.badmintonhub.orderservice.utils.constant.OrderStatusEnum orderStatus;      // CREATED/PROCESSING/...
    private PaymentMethodEnum paymentMethod;  // COD/PAYPAL
    private PaymentStatusEnum paymentStatus;  // UNPAID/PENDING/PAID/...

    private String currency;                  // VND, USD...
    private BigDecimal grandTotal;            // tổng phải trả (sau giảm/ship/tax)
    private BigDecimal shippingFee;           // hiển thị phí ship (nếu cần)

    private ShippingAddress shippingAddress;  // địa chỉ hiển thị ngắn gọn
    private List<Item> items;                 // danh sách item tối thiểu

    private Instant createdAt;                // thời điểm tạo đơn

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShippingAddress {
        private String name;
        private String phone;
        private String oneLine;               // ví dụ: "123 Lê Lợi, P.X, Q.Y, TP.HCM"
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private Long productId;
        private Long optionId;
        private String name;
        private String optionLabel;
        private String image;
        private Integer quantity;

        private BigDecimal unitPrice;         // đơn giá hiển thị (sau giảm)
        private BigDecimal lineTotal;         // thành tiền (sau giảm)
    }
}