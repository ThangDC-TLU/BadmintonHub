package com.badmintonhub.orderservice.service.impl;

import com.badmintonhub.orderservice.dto.mapper.OrderMapper;
import com.badmintonhub.orderservice.dto.mapper.OrderPricingMapper;
import com.badmintonhub.orderservice.dto.message.RestResponse;
import com.badmintonhub.orderservice.dto.model.AddressDTO;
import com.badmintonhub.orderservice.dto.model.ProductItemBriefDTO;
import com.badmintonhub.orderservice.dto.request.CreateOrderRequest;
import com.badmintonhub.orderservice.dto.response.ObjectResponse;
import com.badmintonhub.orderservice.dto.response.OrderResponse;
import com.badmintonhub.orderservice.entity.Order;
import com.badmintonhub.orderservice.exception.IdInvalidException;
import com.badmintonhub.orderservice.repository.OrderRepository;
import com.badmintonhub.orderservice.service.OrderService;
import com.badmintonhub.orderservice.utils.constant.CustomHeaders;
import com.badmintonhub.orderservice.utils.constant.OrderStatusEnum;
import com.badmintonhub.orderservice.utils.constant.PaymentMethodEnum;
import com.badmintonhub.orderservice.utils.constant.PaymentStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static com.badmintonhub.orderservice.utils.GenerateOrderCode.generateOrderCode;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final WebClient webClient;

    // MAPPERS
    private final OrderMapper orderMapper;
    private final OrderPricingMapper pricingMapper;

    public OrderServiceImpl(OrderRepository orderRepository, WebClient webClient, OrderMapper orderMapper, OrderPricingMapper pricingMapper) {
        this.orderRepository = orderRepository;
        this.webClient = webClient;
        this.orderMapper = orderMapper;
        this.pricingMapper = pricingMapper;
    }

    @Override
    @Transactional // rollback khi có lỗi
    public OrderResponse createOrder(long userId, CreateOrderRequest req) {

        // 1) Address (RestResponse<AddressDTO>)
        RestResponse<AddressDTO> addrResp = webClient.get()
                .uri("http://localhost:9000/api/v1/address/{id}", req.getAddressId())
                .retrieve()
                .onStatus(HttpStatusCode::isError, r ->
                        r.bodyToMono(String.class).map(body ->
                                new ResponseStatusException(r.statusCode(), "Address HTTP error: " + body)))
                .bodyToMono(new ParameterizedTypeReference<RestResponse<AddressDTO>>() {})
                .block();

        if (addrResp == null) throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "User service no response");
        if (addrResp.getStatusCode() >= 400)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Address business error: " + addrResp.getMessage());
        AddressDTO addr = addrResp.getData();
        if (addr == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found");

        // 2) Cart (RestResponse<List<ProductItemBriefDTO>>)
        RestResponse<List<ProductItemBriefDTO>> cartResp = webClient.get()
                .uri("http://localhost:8082/api/v1/carts")
                .header(CustomHeaders.X_AUTH_USER_ID, String.valueOf(userId))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::isError, r ->
                        r.bodyToMono(String.class).map(body ->
                                new ResponseStatusException(r.statusCode(), "Cart HTTP error: " + body)))
                .bodyToMono(new ParameterizedTypeReference<RestResponse<List<ProductItemBriefDTO>>>() {})
                .block();

        if (cartResp == null || cartResp.getData() == null)
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Cart service no response");

        List<ProductItemBriefDTO> items = cartResp.getData();
        if (items.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Giỏ hàng trống");

        // 2.1) Lọc theo selectedOptionIds (nếu có)
        Set<Long> selected = (req.getSelectedOptionIds() == null)
                ? Collections.emptySet()
                : new LinkedHashSet<>(req.getSelectedOptionIds());

        if (!selected.isEmpty()) {
            Set<Long> pick = new LinkedHashSet<>(selected);
            List<ProductItemBriefDTO> filtered = items.stream()
                    .filter(i -> i != null && i.getOptionId() != null && pick.contains(i.getOptionId()))
                    .toList();
        }

        // 3) Build Order (snapshot địa chỉ, trạng thái COD)
        Order order = Order.builder()
                .orderCode(generateOrderCode())
                .userId(userId)
                .addressId(addr.getId())
                .shipName(addr.getName())
                .shipPhone(addr.getPhone())
                .shipCompany(addr.getCompany())
                .shipProvince(addr.getProvince())
                .shipDistrict(addr.getDistrict())
                .shipWard(addr.getWard())
                .shipAddress(addr.getAddress())
                .shipAddressType(addr.getAddressType())
                .currency((req.getCurrency() == null || req.getCurrency().isBlank()) ? "VND" : req.getCurrency())
                .orderStatus(OrderStatusEnum.CREATED)
                .paymentMethod(req.getPaymentMethod()) // COD hiện tại
                .paymentStatus(
                        PaymentMethodEnum.COD.equals(req.getPaymentMethod())
                                ? PaymentStatusEnum.UNPAID
                                : PaymentStatusEnum.PENDING)
                .note(req.getNote())
                .build();

        // 4) Dùng PRICING MAPPER: map items -> OrderItem + tính subtotal/discount
        OrderPricingMapper.Totals totals = pricingMapper.attachCartItems(order, items);

        BigDecimal shippingFee = new BigDecimal("20000.00"); // TODO: tính theo chính sách
        BigDecimal taxTotal    = BigDecimal.ZERO;            // TODO: nếu tách VAT
        BigDecimal grandTotal  = totals.getSubtotal()
                .subtract(totals.getDiscount())
                .add(shippingFee)
                .add(taxTotal);

        order.setSubtotal(totals.getSubtotal());
        order.setDiscountTotal(totals.getDiscount());
        order.setShippingFee(shippingFee);
        order.setTaxTotal(taxTotal);
        order.setGrandTotal(grandTotal);

        // 5) Lưu DB
        order = orderRepository.save(order);

        // 6) Dùng ORDER MAPPER: entity -> response gọn
        OrderResponse response = orderMapper.toResponse(order);
        log.info("[CreateOrder] success orderCode={}, grandTotal={}", response.getOrderCode(), response.getGrandTotal());
        return response;
    }

    @Override
    public OrderResponse getOrderById(long id) throws IdInvalidException {
        Order order = this.orderRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Not found order by id " + id));
        return orderMapper.toResponse(order);
    }

    @Override
    public ObjectResponse getAllOrder(Specification<Order> spec, Pageable pageable) {
        Page<Order> page = this.orderRepository.findAll(spec, pageable);
        ObjectResponse objectResponse = new ObjectResponse();
        ObjectResponse.Meta meta = new ObjectResponse.Meta();

        meta.setTotal(page.getTotalElements());
        meta.setPages(page.getTotalPages());

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());

        objectResponse.setMeta(meta);

        List<OrderResponse> orderResponses = page
                .getContent()
                .stream()
                .map(orderMapper::toResponse)
                .toList();
        objectResponse.setResult(orderResponses);
        return objectResponse;
    }

    @Override
    @Transactional
    public String cancelOrderById(long id) throws IdInvalidException {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Not found order by id )" + id));

        // Đã huỷ rồi → idempotent
        if (order.getOrderStatus() == OrderStatusEnum.CANCELLED) {
            return "Order already cancelled";
        }

        // Đã giao cho hãng vận chuyển hoặc đã giao xong → không cho huỷ
        if (order.getOrderStatus() == OrderStatusEnum.SHIPPED
                || order.getOrderStatus() == OrderStatusEnum.DELIVERED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot cancel an order that is shipped or delivered");
        }

        // Đã thanh toán thành công → không cho huỷ (tuỳ chính sách có thể chuyển sang quy trình refund)
        if (order.getPaymentStatus() == PaymentStatusEnum.PAID) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot cancel a paid order");
        }

        // Với CREATED/PROCESSING và chưa PAID → cho huỷ
        order.setOrderStatus(OrderStatusEnum.CANCELLED);

        // Nếu đang chờ thanh toán (PENDING) thì đánh dấu thất bại (FAILED)
        if (order.getPaymentStatus() == PaymentStatusEnum.PENDING) {
            order.setPaymentStatus(PaymentStatusEnum.FAILED);
        }
        orderRepository.save(order);
        return "Order canceled successfully";
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByCode(String orderCode) {
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public ObjectResponse getMyOrders(long userId, OrderStatusEnum status, Pageable pageable) {
        // Build specification: where userId = ? and (status = ? if provided)
        Specification<Order> spec = (root, query, cb) -> {
            var preds = new ArrayList<jakarta.persistence.criteria.Predicate>();
            preds.add(cb.equal(root.get("userId"), userId));
            if (status != null) {
                preds.add(cb.equal(root.get("orderStatus"), status));
            }
            return cb.and(preds.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        Page<Order> page = this.orderRepository.findAll(spec, pageable);
        ObjectResponse objectResponse = new ObjectResponse();
        ObjectResponse.Meta meta = new ObjectResponse.Meta();

        meta.setTotal(page.getTotalElements());
        meta.setPages(page.getTotalPages());

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());

        objectResponse.setMeta(meta);

        List<OrderResponse> orderResponses = page
                .getContent()
                .stream()
                .map(orderMapper::toResponse)
                .toList();
        objectResponse.setResult(orderResponses);
        return objectResponse;
    }

    @Override
    @Transactional
    public OrderResponse updateStatus(long orderId, OrderStatusEnum nextStatus, String note) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        OrderStatusEnum current = order.getOrderStatus();
        if (current == nextStatus) {
            // không đổi gì
            return orderMapper.toResponse(order);
        }

        // Rule chuyển trạng thái đơn giản (có thể chỉnh theo workflow của bạn)
        boolean allowed = switch (current) {
            case CREATED     -> EnumSet.of(OrderStatusEnum.PROCESSING, OrderStatusEnum.CANCELLED).contains(nextStatus);
            case PROCESSING  -> EnumSet.of(OrderStatusEnum.SHIPPED, OrderStatusEnum.CANCELLED).contains(nextStatus);
            case SHIPPED     -> nextStatus == OrderStatusEnum.DELIVERED;
            case DELIVERED   -> false;
            case CANCELLED   -> false;
        };
        if (!allowed) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Illegal transition: " + current + " -> " + nextStatus);
        }

        // Không cho hủy nếu đã PAID
        if (nextStatus == OrderStatusEnum.CANCELLED && order.getPaymentStatus() == PaymentStatusEnum.PAID) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot cancel a paid order");
        }

        order.setOrderStatus(nextStatus);
        if (note != null && !note.isBlank()) {
            order.setNote((order.getNote() == null ? "" : order.getNote() + "\n") + "[STATUS] " + note);
        }

        Order saved = orderRepository.save(order);
        return orderMapper.toResponse(saved);
    }



    @Override
    @Transactional
    public OrderResponse recordCodPayment(long orderId, BigDecimal amount, Instant paidAt, String note) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        if (order.getPaymentMethod() != PaymentMethodEnum.COD) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment method is not COD");
        }
        if (order.getOrderStatus() == OrderStatusEnum.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot record payment for cancelled order");
        }
        if (order.getPaymentStatus() == PaymentStatusEnum.PAID) {
            // idempotent: đã PAID thì trả luôn
            return orderMapper.toResponse(order);
        }
        if (amount == null || amount.signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid amount");
        }
        if (paidAt == null) {
            paidAt = Instant.now();
        }

        // (tuỳ chính sách) xác thực số thu >= grandTotal
        if (order.getGrandTotal() != null && amount.compareTo(order.getGrandTotal()) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount is less than grand total");
        }

        order.setPaymentStatus(PaymentStatusEnum.PAID);
        order.setPaymentId("COD-" + paidAt.toEpochMilli()); // tuỳ bạn: lưu mã phiếu thu/biên nhận
        if (note != null && !note.isBlank()) {
            order.setNote((order.getNote() == null ? "" : order.getNote() + "\n") + "[COD] " + note);
        }

        Order saved = orderRepository.save(order);
        return orderMapper.toResponse(saved);
    }
}


