package com.badmintonhub.orderservice.dto.mapper;

import com.badmintonhub.orderservice.config.ModelMapperConfig;
import com.badmintonhub.orderservice.dto.response.OrderResponse;
import com.badmintonhub.orderservice.entity.Order;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderMapper {
    private final ModelMapper mm;
    private final OrderItemMapper itemMapper;

    public OrderResponse toResponse(Order o) {
        OrderResponse resp = mm.map(o, OrderResponse.class);

        // ShippingAddress (gộp từ các field snapshot)
        OrderResponse.ShippingAddress sa = OrderResponse.ShippingAddress.builder()
                .name(o.getShipName())
                .phone(o.getShipPhone())
                .oneLine(ModelMapperConfig.buildOneLineAddress(o))
                .build();
        resp.setShippingAddress(sa);

        // Items
        resp.setItems(itemMapper.toDtoList(o.getItems()));

        return resp;
    }
}
