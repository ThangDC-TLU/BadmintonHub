package com.badmintonhub.cartservice.service.base;

import java.time.Duration;

public interface BaseRedisService {
    boolean expire(String key, Duration ttl);
    Boolean del(String key);

    Object hGet(String key, String field);

    void hDel(String key, String field);

    Long hIncrBy(String key, String field, long delta);

    /**
     * Tiện ích cho giỏ hàng:
     * - Tăng/giảm quantity bằng HINCRBY (atomic)
     * - Nếu kết quả <= 0 thì xoá field và trả về 0
     * - Optional: gia hạn TTL cho key (vd 30 ngày)
     */
    Long cartIncr(String key, String field, long delta, Duration refreshTtlIfAny);
}
