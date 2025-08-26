package com.badmintonhub.orderservice.service;

import com.badmintonhub.orderservice.dto.model.AddressDTO;
import com.badmintonhub.orderservice.dto.request.CreateOrderRequest;
import com.badmintonhub.orderservice.dto.response.ObjectResponse;
import com.badmintonhub.orderservice.dto.response.OrderResponse;
import com.badmintonhub.orderservice.entity.Order;
import com.badmintonhub.orderservice.exception.IdInvalidException;
import com.badmintonhub.orderservice.utils.constant.OrderStatusEnum;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.Instant;

public interface OrderService {
    OrderResponse createOrder(long userId, CreateOrderRequest createOrderRequest);

    OrderResponse getOrderById(long id) throws IdInvalidException;

    ObjectResponse getAllOrder(Specification<Order> spec, Pageable pageable);

    String cancelOrderById(long id) throws IdInvalidException;

    OrderResponse getOrderByCode(String orderCode);

    ObjectResponse getMyOrders(long userId, OrderStatusEnum status, Pageable pageable);

    OrderResponse updateStatus(long orderId, @NotNull OrderStatusEnum nextStatus, String note);

    OrderResponse recordCodPayment(long orderId, @NotNull BigDecimal amount, @NotNull Instant paidAt, String note);
}
