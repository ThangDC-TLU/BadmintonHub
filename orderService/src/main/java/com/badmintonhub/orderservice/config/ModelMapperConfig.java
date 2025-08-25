package com.badmintonhub.orderservice.config;


import com.badmintonhub.orderservice.dto.response.OrderResponse;
import com.badmintonhub.orderservice.entity.Order;
import com.badmintonhub.orderservice.entity.OrderItem;
import org.modelmapper.*;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mm = new ModelMapper();
        mm.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setSkipNullEnabled(true)
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE);

        // Converter: Double -> BigDecimal(scale=2)
        Converter<Double, BigDecimal> dbl2bd = ctx -> {
            Double v = ctx.getSource();
            if (v == null) return null;
            return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP);
        };
        mm.addConverter(dbl2bd);

        // TypeMap: OrderItem -> OrderResponse.Item
        mm.typeMap(OrderItem.class, OrderResponse.Item.class)
                .addMapping(OrderItem::getProductId, OrderResponse.Item::setProductId)
                .addMapping(OrderItem::getOptionId,  OrderResponse.Item::setOptionId)
                .addMapping(OrderItem::getNameSnapshot, OrderResponse.Item::setName)
                .addMapping(OrderItem::getOptionLabelSnapshot, OrderResponse.Item::setOptionLabel)
                .addMapping(OrderItem::getImageSnapshot, OrderResponse.Item::setImage)
                .addMapping(OrderItem::getQuantity, OrderResponse.Item::setQuantity)
                // hiển thị đơn giá/line total là GIÁ SAU GIẢM
                .addMapping(OrderItem::getUnitDiscountedPrice, OrderResponse.Item::setUnitPrice)
                .addMapping(OrderItem::getLineDiscountedPrice, OrderResponse.Item::setLineTotal);

        // TypeMap: Order -> OrderResponse (map các trường đơn giản)
        mm.typeMap(Order.class, OrderResponse.class)
                .addMapping(Order::getOrderCode, OrderResponse::setOrderCode)
                .addMapping(Order::getOrderStatus, OrderResponse::setOrderStatus)
                .addMapping(Order::getPaymentMethod, OrderResponse::setPaymentMethod)
                .addMapping(Order::getPaymentStatus, OrderResponse::setPaymentStatus)
                .addMapping(Order::getCurrency, OrderResponse::setCurrency)
                .addMapping(Order::getGrandTotal, OrderResponse::setGrandTotal)
                .addMapping(Order::getShippingFee, OrderResponse::setShippingFee)
                .addMapping(Order::getCreatedAt, OrderResponse::setCreatedAt);

        return mm;
    }

    /** Build một dòng địa chỉ từ Order (để set vào ShippingAddress.oneLine) */
    public static String buildOneLineAddress(Order o) {
        StringBuilder sb = new StringBuilder();
        append(sb, o.getShipAddress());
        append(sb, o.getShipWard());
        append(sb, o.getShipDistrict());
        append(sb, o.getShipProvince());
        return sb.toString();
    }
    private static void append(StringBuilder sb, String v) {
        if (v == null || v.isBlank()) return;
        if (sb.length() > 0) sb.append(", ");
        sb.append(v);
    }
}
