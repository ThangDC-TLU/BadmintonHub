package com.badmintonhub.orderservice.controller;

import com.badmintonhub.orderservice.dto.model.AddressDTO;
import com.badmintonhub.orderservice.dto.request.CreateOrderRequest;
import com.badmintonhub.orderservice.dto.response.OrderResponse;
import com.badmintonhub.orderservice.service.OrderService;
import com.badmintonhub.orderservice.utils.anotation.ApiMessage;
import com.badmintonhub.orderservice.utils.constant.CustomHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/order")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ApiMessage("Create order")
    public ResponseEntity<OrderResponse> createOrder(@RequestHeader(CustomHeaders.X_AUTH_USER_ID) long userId,
                                                  @RequestBody CreateOrderRequest createOrderRequest) {
        return ResponseEntity.ok(this.orderService.createOrder(userId, createOrderRequest));
    }
}
