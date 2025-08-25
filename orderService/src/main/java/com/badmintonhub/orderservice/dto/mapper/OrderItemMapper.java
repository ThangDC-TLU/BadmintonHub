package com.badmintonhub.orderservice.dto.mapper;

import com.badmintonhub.orderservice.dto.response.OrderResponse;
import com.badmintonhub.orderservice.entity.OrderItem;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderItemMapper {
    private final ModelMapper mm;

    public OrderResponse.Item toDto(OrderItem item) {
        return mm.map(item, OrderResponse.Item.class);
    }

    public List<OrderResponse.Item> toDtoList(List<OrderItem> items) {
        return items.stream().map(this::toDto).toList();
    }
}
