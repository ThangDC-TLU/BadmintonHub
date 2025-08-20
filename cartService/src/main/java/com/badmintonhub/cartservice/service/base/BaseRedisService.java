package com.badmintonhub.cartservice.service.base;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface BaseRedisService {

    // -------- Key-Value (String) --------
    void set(String key, String value);
    String getString(String key);

    // -------- TTL / Key ops --------
    boolean expireDays(String key, long days);
    boolean expire(String key, Duration ttl);
    Long ttlSeconds(String key);        // trả TTL còn lại (giây), -1/-2 theo Redis
    Boolean del(String key);
    Boolean exists(String key);

    Set<String> hKeys(String key);
    Long hSize(String key);

    void hDel(String key, String field);
    void hDel(String key, List<String> fields);

    Long hIncrBy(String key, String field, long delta);

    /**
     * Tiện ích cho giỏ hàng:
     * - Tăng/giảm quantity bằng HINCRBY (atomic)
     * - Nếu kết quả <= 0 thì xoá field và trả về 0
     * - Optional: gia hạn TTL cho key (vd 30 ngày)
     */
    Long cartIncr(String key, String field, long delta, Duration refreshTtlIfAny);
}
