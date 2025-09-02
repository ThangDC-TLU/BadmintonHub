package com.badmintonhub.orderservice.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PaypalOrderCreateRequest {

    /**
     * "CAPTURE" (thanh toán ngay) hoặc "AUTHORIZE" (uỷ quyền, capture sau)
     */
    private String intent;

    private List<PurchaseUnit> purchaseUnits;

    private PaymentSource paymentSource;


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class PurchaseUnit {
        private String referenceId;   // mã tham chiếu đơn hàng nội bộ
        private Amount amount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Amount {
        private String currencyCode;  // ví dụ: "USD", "EUR", "VND"
        private String value;         // ví dụ: "49.90"
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentSource {
        private Paypal paypal;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Paypal {
        private ExperienceContext experienceContext;
    }

    /**
     * Điều khiển trải nghiệm checkout (đường dẫn quay lại, thương hiệu, ngôn ngữ...)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ExperienceContext {
        private String returnUrl;                 // bắt buộc nếu dùng redirect approve
        private String cancelUrl;                 // bắt buộc nếu dùng redirect approve
        private String brandName;                 // "BadmintonHub"
        private String userAction;                // "PAY_NOW" hoặc "CONTINUE"
        private String shippingPreference;        // "NO_SHIPPING" hoặc "SET_PROVIDED_ADDRESS"
        private String locale;                    // "vi-VN", "en-US"...
        private String paymentMethodPreference;   // "IMMEDIATE_PAYMENT_REQUIRED" (khuyến nghị)
    }
}
