package com.badmintonhub.orderservice.controller;

import com.badmintonhub.orderservice.dto.request.*;
import com.badmintonhub.orderservice.dto.response.ObjectResponse;
import com.badmintonhub.orderservice.dto.response.OrderResponse;
import com.badmintonhub.orderservice.entity.Order;
import com.badmintonhub.orderservice.exception.IdInvalidException;
import com.badmintonhub.orderservice.service.OrderService;
import com.badmintonhub.orderservice.utils.anotation.ApiMessage;
import com.badmintonhub.orderservice.utils.constant.CustomHeaders;
import com.badmintonhub.orderservice.utils.constant.OrderStatusEnum;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /** TẠO ĐƠN (COD trước) */
    @PostMapping
    @ApiMessage("Create order")
    public ResponseEntity<OrderResponse> createOrder(
            @RequestHeader(CustomHeaders.X_AUTH_USER_ID) long userId,
            @Valid @RequestBody CreateOrderRequest req
    ) {
        return ResponseEntity.ok(orderService.createOrder(userId, req));
    }

    /** LẤY CHI TIẾT ĐƠN THEO ID */
    @GetMapping("/{orderId}")
    @ApiMessage("Get order by id")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable long orderId) throws IdInvalidException {
        return ResponseEntity.ok(orderService.getOrderById(orderId));
    }

    /** LẤY CHI TIẾT ĐƠN THEO MÃ (tiện tra cứu) */
    @GetMapping("/by-code/{orderCode}")
    @ApiMessage("Get order by code")
    public ResponseEntity<OrderResponse> getOrderByCode(@PathVariable String orderCode) {
        return ResponseEntity.ok(orderService.getOrderByCode(orderCode));
    }

    /** DANH SÁCH ĐƠN CỦA TÔI (KH) */
    @GetMapping("/my")
    @ApiMessage("Get my orders")
    public ResponseEntity<ObjectResponse> getMyOrders(
            @RequestHeader(CustomHeaders.X_AUTH_USER_ID) long userId,
            @RequestParam(value = "status", required = false) OrderStatusEnum status,
            Pageable pageable
    ) {
        return ResponseEntity.ok(orderService.getMyOrders(userId, status, pageable));
    }

    /** DANH SÁCH TẤT CẢ ĐƠN (ADMIN/CSKH) */
    @GetMapping
    @ApiMessage("Get all orders (admin)")
    public ResponseEntity<ObjectResponse> getAllOrders(
            @Filter Specification<Order> spec,
            Pageable pageable
    ) {
        return ResponseEntity.ok(orderService.getAllOrder(spec, pageable));
    }

    /** HỦY ĐƠN (soft-cancel) – cho chủ đơn hoặc CSKH; không xoá cứng */
    @PostMapping("/{orderId}/cancel")
    @ApiMessage("Cancel order")
    public ResponseEntity<String> cancelOrder(@PathVariable long orderId) throws IdInvalidException {
        return ResponseEntity.ok(orderService.cancelOrderById(orderId));
    }

    /* ========= Các API nội bộ/tuỳ chọn cho COD (bật khi cần) ========= */

    /** ADMIN: cập nhật trạng thái */
    @PatchMapping("/{orderId}/status")
    @ApiMessage("Update order status (admin)")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable long orderId,
            @RequestBody @Valid UpdateStatusRequest body
    ) {
        return ResponseEntity.ok(orderService.updateStatus(orderId, body.getNextStatus(), body.getNote()));
    }

    /** ADMIN/CSKH: ghi nhận thu COD → PAID */
    @PostMapping("/{orderId}/payment/cod")
    @ApiMessage("Record COD payment (admin)")
    public ResponseEntity<OrderResponse> recordCodPayment(
            @PathVariable long orderId,
            @RequestBody @Valid RecordCodPaymentRequest body
    ) {
        return ResponseEntity.ok(orderService.recordCodPayment(orderId, body.getAmount(), body.getPaidAt(), body.getNote()));
    }

    @GetMapping("/payments/paypal/return")
    public org.springframework.http.ResponseEntity<OrderResponse> onReturn(
            @RequestParam("token") String paypalOrderId // PayPal trả token=orderId
    ) {
        return ResponseEntity.ok(orderService.capturePaypalOrder(paypalOrderId));
    }

    @GetMapping("/payments/paypal/cancel")
    public org.springframework.http.ResponseEntity<String> onCancel(
            @RequestParam("token") String paypalOrderId
    ) {
        orderService.cancelPaypalOrder(paypalOrderId);
        return ResponseEntity.ok("Cancelled on PayPal");
    }

}
