package com.badmintonhub.cartservice.service.impl;

import com.badmintonhub.cartservice.dto.message.RestResponse;
import com.badmintonhub.cartservice.dto.model.ProductItemBriefDTO;
import com.badmintonhub.cartservice.dto.request.CartItemRequest;
import com.badmintonhub.cartservice.service.CartRedisService;
import com.badmintonhub.cartservice.service.base.BaseRedisService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class CartRedisServiceImpl implements CartRedisService {
    private final BaseRedisService baseRedisService;

    private static final String FIELD_PREFIX = "product_item:";
    private final WebClient.Builder lbWebClient;

    public CartRedisServiceImpl(BaseRedisService baseRedisService, WebClient.Builder lbWebClient) {
        this.baseRedisService = baseRedisService;
        this.lbWebClient = lbWebClient;
    }

    @Override
    public void addProductToCart(String userId, CartItemRequest item) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId is required");
        }
        if (item == null || item.getProductId() == null) {
            throw new IllegalArgumentException("productId is required");
        }
        if (item.getQuantity() == 0) {
            // Không làm gì khi quantity = 0
            return;
        }

        // 1) Key và field
        final String key = "cart:user-" + userId;
        final String fieldKey = buildFieldKey(item);

        // 2) Atomic increment
        Long newQty = this.baseRedisService.hIncrBy(key, fieldKey, item.getQuantity());

        // 3) Nếu ≤ 0 thì xóa field
        if (newQty != null && newQty <= 0) {
            this.baseRedisService.hDel(key, fieldKey);
        }

        // 4) Optional TTL gia hạn 30 ngày
        this.baseRedisService.expire(key, Duration.ofDays(30));
    }

    @Override
    public void updateProductQuantity(String userId, CartItemRequest item) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId is required");
        }
        if (item == null || item.getProductId() == null) {
            throw new IllegalArgumentException("productId is required");
        }
        // 1) Key và field
        final String key = "cart:user-" + userId;
        final String fieldKey = buildFieldKey(item);

        //Lấy số lượng sản phẩm
        Integer current = (Integer) this.baseRedisService.hGet(key, fieldKey);

        //nếu có thì tính toán, nếu ko thì thêm mới
        int curr = current == null ? 0 : current;
        int delta = item.getQuantity() - curr;
        this.baseRedisService.cartIncr(key,fieldKey, delta, Duration.ofDays(30));
    }

    @Override
    public void removeProductFromCart(String userId, CartItemRequest item) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId is required");
        }
        if (item == null || item.getProductId() == null) {
            throw new IllegalArgumentException("productId is required");
        }
        String key = "cart:user-" + userId;
        String field = buildFieldKey(item);
        this.baseRedisService.hDel(key, field);
    }

    @Override
    public void clearCart(String userId) {
        String key = "cart:user-" + userId;
        this.baseRedisService.del(key);
    }

    @CircuitBreaker(name = "productClient", fallbackMethod = "getProductFallback")
    @Retry(name = "productClient")
    @Override
    public List<ProductItemBriefDTO> getProductFromCart(String userId) {
        final String key = "cart:user-" + userId;
        final Pattern PRODUCT_ITEM_PATTERN = Pattern.compile("^product_item:(\\d+):(\\d+)$"); // group1=productId, group2=optionId

        // 1) Lấy toàn bộ field/value trong hash giỏ hàng
        Map<String, Object> products = this.baseRedisService.getField(key);
        if (products == null || products.isEmpty()) {
            return List.of();
        }

        // 2) Trích xuất optionId (giữ thứ tự gặp được)
        LinkedHashSet<Long> optionIdSet = new LinkedHashSet<>();
        Map<Long, Integer> qtyByOption = new LinkedHashMap<>();

        for (Map.Entry<String, Object> e : products.entrySet()) {
            String field = e.getKey();
            if (field == null) continue;

            Matcher m = PRODUCT_ITEM_PATTERN.matcher(field);
            if (!m.matches()) continue;

            Long optionId = Long.valueOf(m.group(2)); // lấy optionId
            optionIdSet.add(optionId);

            int qty = asInt(e.getValue());
            qtyByOption.merge(optionId, qty, Integer::sum);
        }

        if (optionIdSet.isEmpty()) {
            return List.of();
        }
        List<Long> optionIds = new ArrayList<>(optionIdSet);

        // 3) Gọi product-service để lấy Map<optionId, ProductItemBriefDTO>
        RestResponse<Map<Long, ProductItemBriefDTO>> resp = lbWebClient
                .baseUrl("http://PRODUCT-SERVICE")
                .build()
                .post()
                .uri("/api/v1/products/product-options")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(optionIds)
                .retrieve()
                .onStatus(HttpStatusCode::isError, r ->
                        r.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException(
                                        "PRODUCT-SERVICE HTTP %s: %s".formatted(r.statusCode(), body)))))
                .bodyToMono(new ParameterizedTypeReference<RestResponse<Map<Long, ProductItemBriefDTO>>>() {})
                .timeout(Duration.ofSeconds(3))
                .block(); // exception sẽ bung ra -> CB nhìn thấy -> fallback


        if (resp == null) {
            return List.of();
        }
        if (resp.getStatusCode() >= 400) {
            // tuỳ bạn: ném exception hoặc log rồi trả rỗng
            throw new RuntimeException("PRODUCT-SERVICE error: " + resp.getStatusCode() + " - " + resp.getMessage());
        }

        Map<Long, ProductItemBriefDTO> productMap = resp.getData();
        if (productMap == null || productMap.isEmpty()) {
            return List.of();
        }

        if (productMap == null || productMap.isEmpty()) {
            return List.of();
        }

        // 4) Trả list theo thứ tự optionIds; đảm bảo giá đã tính đầy đủ
        List<ProductItemBriefDTO> result = new ArrayList<>(optionIds.size());
        for (Long id : optionIds) {
            ProductItemBriefDTO dto = productMap.get(id);
            if (dto == null) continue;

            int qty = qtyByOption.getOrDefault(id, 0);
            dto.setQuantity(qty);
            ensureComputedPrices(dto,qty);
            result.add(dto);
        }

        return result;
    }

    // Fallback
    private List<ProductItemBriefDTO> getProductFallback(String userId, Throwable ex) {
        log.error("ProductFallback userId={}, cause={}", userId, ex.toString(), ex);
        return List.of(); // degrade
    }

    private static int asInt(Object v) {
        if (v == null) return 0;
        if (v instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(String.valueOf(v));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private double round2(double v) {
        return new java.math.BigDecimal(v).setScale(2, java.math.RoundingMode.HALF_UP).doubleValue();
    }

    private void ensureComputedPrices(ProductItemBriefDTO p, int quantity) {
        double unitFinal = p.getFinalPrice() > 0
                ? p.getFinalPrice()
                : (p.getBasePrice() + p.getAddPrice() - p.getSubPrice());
        p.setFinalPrice(round2(unitFinal));

        double factor = 1.0 - (p.getDiscountPercent() / 100.0);
        if (factor < 0) factor = 0;
        double unitDisc = p.getDiscountedFinalPrice() > 0
                ? p.getDiscountedFinalPrice()
                : (unitFinal * factor);
        p.setDiscountedFinalPrice(round2(unitDisc));

        p.setQuantity(quantity);
        p.setLineFinalPrice(round2(unitFinal * quantity));
        p.setLineDiscountedFinalPrice(round2(unitDisc * quantity));
    }



    private String buildFieldKey(CartItemRequest item) {
        if (item.getOptionId() != null) {
            // Nếu có optionId -> lưu theo dạng product_item:{productId}:{optionId}
            return "product_item:" + item.getProductId() + ":" + item.getOptionId();
        }
        // Nếu không có optionId -> chỉ lưu product:{productId}
        return "product:" + item.getProductId();
    }

}
