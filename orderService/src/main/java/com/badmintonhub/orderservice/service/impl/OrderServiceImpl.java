package com.badmintonhub.orderservice.service.impl;

import com.badmintonhub.orderservice.dto.mapper.OrderMapper;
import com.badmintonhub.orderservice.dto.mapper.OrderPlacedEventMapper;
import com.badmintonhub.orderservice.dto.mapper.OrderPricingMapper;
import com.badmintonhub.orderservice.dto.message.RestResponse;
import com.badmintonhub.orderservice.dto.model.AddressDTO;
import com.badmintonhub.orderservice.dto.model.ProductItemBriefDTO;
import com.badmintonhub.orderservice.dto.request.CreateOrderRequest;
import com.badmintonhub.orderservice.dto.response.ObjectResponse;
import com.badmintonhub.orderservice.dto.response.OrderResponse;
import com.badmintonhub.orderservice.entity.Order;
import com.badmintonhub.orderservice.dto.event.OrderPlacedEvent;
import com.badmintonhub.orderservice.exception.IdInvalidException;
import com.badmintonhub.orderservice.paypal.PaypalClient;
import com.badmintonhub.orderservice.dto.request.PaypalOrderCreateRequest;
import com.badmintonhub.orderservice.dto.response.PaypalOrderCreateResponse;
import com.badmintonhub.orderservice.repository.OrderRepository;
import com.badmintonhub.orderservice.service.OrderExternalService;
import com.badmintonhub.orderservice.service.OrderService;
import com.badmintonhub.orderservice.utils.FxConverter;
import com.badmintonhub.orderservice.utils.constant.OrderStatusEnum;
import com.badmintonhub.orderservice.utils.constant.PaymentMethodEnum;
import com.badmintonhub.orderservice.utils.constant.PaymentStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
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
    private final FxConverter fxConverter;
    private final OrderMapper orderMapper;
    private final OrderPricingMapper pricingMapper;
    private final PaypalClient paypalClient;
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;
    private final OrderExternalService  orderExternalService;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            FxConverter fxConverter,
            OrderMapper orderMapper,
            OrderPricingMapper pricingMapper,
            PaypalClient paypalClient,
            KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate,
            OrderExternalService orderExternalService)
    {
        this.orderRepository = orderRepository;
        this.fxConverter = fxConverter;
        this.orderMapper = orderMapper;
        this.pricingMapper = pricingMapper;
        this.paypalClient = paypalClient;
        this.kafkaTemplate = kafkaTemplate;
        this.orderExternalService = orderExternalService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // rollback cho cả checked exceptions nếu có
    public OrderResponse createOrder(long userId, CreateOrderRequest req) throws IdInvalidException {
        // 1) Lấy địa chỉ
        RestResponse<AddressDTO> addrResp = this.orderExternalService.getAddressById(userId);
        if (addrResp == null) throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "User service no response");
        if (addrResp.getStatusCode() >= 400)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Address business error: " + addrResp.getMessage());
        AddressDTO addr = addrResp.getData();
        if (addr == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found");

        // 2) Lấy cart
        RestResponse<List<ProductItemBriefDTO>> cartResp = this.orderExternalService.getCart(userId);
        if (cartResp == null || cartResp.getData() == null)
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Cart service no response");

        List<ProductItemBriefDTO> items = cartResp.getData();
        if (items.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Giỏ hàng trống");

        // 2.1) Lọc theo selectedOptionIds
        Set<Long> selected = (req.getSelectedOptionIds() == null)
                ? Collections.emptySet()
                : new LinkedHashSet<>(req.getSelectedOptionIds());
        if (!selected.isEmpty()) {
            items = items.stream()
                    .filter(i -> i != null && i.getOptionId() != null && selected.contains(i.getOptionId()))
                    .toList();
            if (items.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Không có item nào hợp lệ để checkout (selectedOptionIds không khớp)");
            }
        }

        // 3) Build Order
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
                .paymentMethod(req.getPaymentMethod())
                .paymentStatus(PaymentMethodEnum.COD.equals(req.getPaymentMethod())
                        ? PaymentStatusEnum.UNPAID : PaymentStatusEnum.PENDING)
                .note(req.getNote())
                .build();

        // 4) Map items + tính tổng
        OrderPricingMapper.Totals totals = pricingMapper.attachCartItems(order, items);
        BigDecimal shippingFee = new BigDecimal("20000.00");
        BigDecimal taxTotal    = BigDecimal.ZERO;
        BigDecimal grandTotal  = totals.getSubtotal()
                .subtract(totals.getDiscount())
                .add(shippingFee)
                .add(taxTotal);

        order.setSubtotal(totals.getSubtotal());
        order.setDiscountTotal(totals.getDiscount());
        order.setShippingFee(shippingFee);
        order.setTaxTotal(taxTotal);
        order.setGrandTotal(grandTotal);

        // Dùng biến để chuẩn bị gửi event SAU COMMIT
        final AddressDTO fAddr = addr;

        // ===== 5) PAYPAL =====
        if (PaymentMethodEnum.PAYPAL.equals(req.getPaymentMethod())) {
            if (req.getReturnUrl() == null || req.getCancelUrl() == null
                    || req.getReturnUrl().isBlank() || req.getCancelUrl().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "returnUrl/cancelUrl là bắt buộc cho thanh toán PayPal");
            }

            try {
                var exp = PaypalOrderCreateRequest.ExperienceContext.builder()
                        .returnUrl(req.getReturnUrl())
                        .cancelUrl(req.getCancelUrl())
                        .userAction("PAY_NOW")
                        .build();

                String paypalCurrency = "USD";
                BigDecimal amountUsd = fxConverter.vndToUsd(order.getGrandTotal());

                // Gọi PayPal → nếu lỗi, ném runtime để rollback
                PaypalOrderCreateResponse p = this.orderExternalService
                        .createPaypalOrder(order.getOrderCode(), amountUsd, paypalCurrency, exp)
                        .getData();
                if (p == null || p.getId() == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Cannot create PayPal order");
                }

                order.setPaymentId(p.getId());
                order = orderRepository.save(order); // lưu vào DB trong cùng transaction

                String approvalUrl = (p.getLinks() == null) ? null :
                        p.getLinks().stream()
                                .filter(l -> "approve".equalsIgnoreCase(l.getRel()) || "payer-action".equalsIgnoreCase(l.getRel()))
                                .map(PaypalOrderCreateResponse.Link::getHref)
                                .findFirst().orElse(null);

                OrderResponse response = orderMapper.toResponse(order);
                response.setApprovalUrl(approvalUrl);

                OrderPlacedEvent event = OrderPlacedEventMapper.fromOrderResponse(
                        response,
                        fAddr.getEmail(),
                        fAddr.getName(),
                        "vi-VN",
                        "Asia/Ho_Chi_Minh",
                        "https://badmintonhub.vn/orders/" + order.getOrderCode(),
                        "https://badmintonhub.vn/track/" + order.getOrderCode(),
                        userId
                );
                event.setPaymentMethod(PaymentMethodEnum.PAYPAL);

                // CHỈ gửi Kafka SAU KHI COMMIT DB
                Order finalOrder = order;
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override public void afterCommit() {
                        kafkaTemplate.send("order-topic", event);
                        log.info("[CreateOrder][PAYPAL][AFTER_COMMIT] sent Kafka event for orderCode={}, paymentID={}", response.getOrderCode(), finalOrder.getPaymentId());
                    }
                });

                log.info("[CreateOrder][PAYPAL] orderCode={}, grandTotal={}, approvalUrl={}",
                        response.getOrderCode(), response.getGrandTotal(), approvalUrl);
                return response;

            } catch (ResponseStatusException e) {
                throw e; // giữ nguyên để rollback
            } catch (Exception e) {
                // Bọc lỗi sang Runtime để rollback
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "PayPal create order failed", e);
            }
        }

        // ===== 6) COD =====
        order = orderRepository.save(order);
        OrderResponse response = orderMapper.toResponse(order);
        log.info("[CreateOrder][COD] orderCode={}, grandTotal={}", response.getOrderCode(), response.getGrandTotal());

        OrderPlacedEvent event = OrderPlacedEventMapper.fromOrderResponse(
                response,
                fAddr.getEmail(),
                fAddr.getName(),
                "vi-VN",
                "Asia/Ho_Chi_Minh",
                "https://badmintonhub.vn/orders/" + order.getOrderCode(),
                "https://badmintonhub.vn/track/" + order.getOrderCode(),
                userId
        );
        event.setPaymentMethod(PaymentMethodEnum.COD);

        // CHỈ gửi Kafka SAU KHI COMMIT DB
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() {
                kafkaTemplate.send("order-topic", event);
                log.info("[CreateOrder][COD][AFTER_COMMIT] sent Kafka event for orderCode={}", response.getOrderCode());
            }
        });

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

        // Rule chuyển trạng thái
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

        // xác thực số thu >= grandTotal
        if (order.getGrandTotal() != null && amount.compareTo(order.getGrandTotal()) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount is less than grand total");
        }

        order.setPaymentStatus(PaymentStatusEnum.PAID);
        order.setPaymentId("COD-" + paidAt.toEpochMilli()); //lưu mã phiếu thu/biên nhận
        if (note != null && !note.isBlank()) {
            order.setNote((order.getNote() == null ? "" : order.getNote() + "\n") + "[COD] " + note);
        }

        Order saved = orderRepository.save(order);
        return orderMapper.toResponse(saved);
    }


    @Override
    @Transactional
    public OrderResponse capturePaypalOrder(String paypalOrderId) {
        PaypalOrderCreateResponse captureBody = paypalClient.captureOrder(paypalOrderId);
        log.info(captureBody.toString());
        if (captureBody == null || !captureBody.getStatus().contains("COMPLETED")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "PayPal capture not completed");
        }

        Order order = orderRepository.findByPaymentId(paypalOrderId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found for PayPal id"));

        order.setPaymentStatus(PaymentStatusEnum.PAID);
        if (order.getOrderStatus() == OrderStatusEnum.CREATED) {
            order.setOrderStatus(OrderStatusEnum.PROCESSING);
        }
        order = orderRepository.save(order);
        return orderMapper.toResponse(order);
    }


    @Override
    @Transactional
    public void cancelPaypalOrder(String paypalOrderId) {
        orderRepository.findByPaymentId(paypalOrderId).ifPresent(o -> {
            if (o.getPaymentStatus() == PaymentStatusEnum.PENDING) {
                o.setPaymentStatus(PaymentStatusEnum.FAILED);
                orderRepository.save(o);
            }
        });
    }

}


