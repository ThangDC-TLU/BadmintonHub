package com.badmintonhub.orderservice.service;

import com.badmintonhub.orderservice.dto.model.AddressDTO;
import com.badmintonhub.orderservice.dto.request.CreateOrderRequest;
import com.badmintonhub.orderservice.dto.response.OrderResponse;

public interface OrderService {
    AddressDTO createOrder(long userId, CreateOrderRequest createOrderRequest);
}
