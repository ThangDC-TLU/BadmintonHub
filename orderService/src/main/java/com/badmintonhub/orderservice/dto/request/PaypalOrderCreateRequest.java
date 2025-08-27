package com.badmintonhub.orderservice.dto.request;

import lombok.Builder;
import lombok.Data;

import java.util.List;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@Data @Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PaypalOrderCreateRequest {
    private String intent; // "CAPTURE" hoặc "AUTHORIZE"
    private List<PurchaseUnit> purchaseUnits;
    private ApplicationContext applicationContext;
    private PaymentSource paymentSource;

    @Data @Builder
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class PurchaseUnit {
        private String referenceId;
        private Amount amount;
    }

    @Data @Builder
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Amount {
        private String currencyCode;
        private String value;
    }

    // Cách “cũ” nhưng đơn giản
    @Data @Builder
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ApplicationContext {
        private String returnUrl;   // BẮT BUỘC nếu cần user approve
        private String cancelUrl;   // BẮT BUỘC
        private String brandName;   // tùy chọn
        private String userAction;  // "PAY_NOW"
        private String shippingPreference; // "NO_SHIPPING" nếu không ship
        private String locale;      // "en-US"…
    }

    // Cách “mới”
    @Data @Builder
    public static class PaymentSource {
        private Paypal paypal;
    }
    @Data @Builder
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Paypal {
        private ExperienceContext experienceContext;
    }
    @Data @Builder
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ExperienceContext {
        private String returnUrl;
        private String cancelUrl;
        private String brandName;
        private String userAction;          // "PAY_NOW"
        private String shippingPreference;  // "NO_SHIPPING"
        private String locale;
        private String paymentMethodPreference; // "IMMEDIATE_PAYMENT_REQUIRED"
    }
}
