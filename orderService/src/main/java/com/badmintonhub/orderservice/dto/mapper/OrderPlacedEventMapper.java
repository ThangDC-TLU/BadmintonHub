package com.badmintonhub.orderservice.dto.mapper;

import com.badmintonhub.orderservice.dto.response.OrderResponse;
import com.badmintonhub.orderservice.dto.event.OrderPlacedEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class OrderPlacedEventMapper {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private OrderPlacedEventMapper() {}

    public static OrderPlacedEvent fromOrderResponse(
            OrderResponse or,
            String customerEmail,
            String customerName,
            String locale,               // ví dụ "vi-VN"
            String timezone,             // ví dụ "Asia/Ho_Chi_Minh"
            String orderDetailUrl,       // link FE xem đơn
            String trackingUrl,          // link theo dõi
            Long userId                // id người dùng (nếu có)
    ) {
        // subtotal = tổng lineTotal; nếu null thì unitPrice * quantity
        BigDecimal subtotal = or.getItems() == null ? ZERO :
                or.getItems().stream()
                        .map(i -> coalesce(i.getLineTotal(),
                                safeMul(i.getUnitPrice(), i.getQuantity())))
                        .reduce(ZERO, BigDecimal::add);

        BigDecimal shippingFee = coalesce(or.getShippingFee(), ZERO);
        BigDecimal grandTotal  = coalesce(or.getGrandTotal(), subtotal.add(shippingFee));

        // Có thể suy ra discount (nếu cần), để null nếu không chắc
        BigDecimal discountTotal = null; // tùy bạn có muốn compute hay không
        BigDecimal taxTotal      = null;

        return OrderPlacedEvent.builder()
                // metadata
                .eventId(UUID.randomUUID().toString())
                .type("order.placed")
                .version("v1")
                .timestamp(Instant.now())

                // người nhận
                .userId(userId)
                .customerEmail(customerEmail)
                .customerName(customerName)
                .locale(locale)
                .timezone(timezone)

                // đơn hàng
                .orderCode(or.getOrderCode())
                .createdAt(or.getCreatedAt())
                .currency(or.getCurrency())
                .subtotal(subtotal)
                .discountTotal(discountTotal)
                .shippingFee(shippingFee)
                .taxTotal(taxTotal)
                .grandTotal(grandTotal)
                .paymentMethod(or.getPaymentMethod())
                .paymentStatus(or.getPaymentStatus())
                .approvalUrl(or.getApprovalUrl())

                // địa chỉ & items
                .shippingAddress(mapAddress(or.getShippingAddress()))
                .items(mapItems(or.getItems()))

                // links
                .orderDetailUrl(orderDetailUrl)
                .trackingUrl(trackingUrl)

                .meta(Map.of("orderStatus", String.valueOf(or.getOrderStatus())))
                .build();
    }

    private static OrderPlacedEvent.ShippingAddress mapAddress(OrderResponse.ShippingAddress a) {
        if (a == null) return null;
        return OrderPlacedEvent.ShippingAddress.builder()
                .name(a.getName())
                .phone(a.getPhone())
                .oneLine(a.getOneLine())
                .build();
    }

    private static List<OrderPlacedEvent.Item> mapItems(List<OrderResponse.Item> items) {
        if (items == null) return List.of();
        return items.stream().map(i -> OrderPlacedEvent.Item.builder()
                        .productId(i.getProductId())
                        .optionId(i.getOptionId())
                        .name(i.getName())
                        .optionLabel(i.getOptionLabel())
                        .image(i.getImage())
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .lineTotal(coalesce(i.getLineTotal(), safeMul(i.getUnitPrice(), i.getQuantity())))
                        .build())
                .toList();
    }

    private static BigDecimal coalesce(BigDecimal v, BigDecimal def) {
        return v == null ? def : v;
    }
    private static BigDecimal safeMul(BigDecimal unitPrice, Integer qty) {
        if (unitPrice == null || qty == null) return ZERO;
        return unitPrice.multiply(BigDecimal.valueOf(qty));
    }
}
