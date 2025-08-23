package com.badmintonhub.orderservice.dto.request;

import com.badmintonhub.orderservice.utils.constant.PaymentMethodEnum;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class CreateOrderRequest {
    @NotNull
    Long addressId;          // địa chỉ giao hàng đã chọn
    @Size(max=2000) String note;      // optional
    PaymentMethodEnum paymentMethod;  // COD/PAYPAL...
    @Builder.Default
    String currency = "VND";
    // optional: nếu muốn “checkout các item được chọn” trong giỏ
    List<Long> selectedOptionIds;     // null => lấy toàn bộ giỏ
}

