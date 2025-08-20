package com.badmintonhub.cartservice.service.impl;

import com.badmintonhub.cartservice.dto.request.CartItemRequest;
import com.badmintonhub.cartservice.service.CartRedisService;
import com.badmintonhub.cartservice.service.base.BaseRedisService;
import org.springframework.stereotype.Service;

@Service
public class CartRedisServiceImpl implements CartRedisService {
    private final BaseRedisService baseRedisService;
    public CartRedisServiceImpl(BaseRedisService baseRedisService) {
        this.baseRedisService = baseRedisService;
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
        this.baseRedisService.expire(key, java.time.Duration.ofDays(30));
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
