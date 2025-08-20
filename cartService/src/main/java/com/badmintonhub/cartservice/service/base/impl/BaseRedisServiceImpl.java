package com.badmintonhub.cartservice.service.base.impl;

import com.badmintonhub.cartservice.service.base.BaseRedisService;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class BaseRedisServiceImpl implements BaseRedisService {
    private final RedisTemplate<String, Object> redisTemplate;

    public BaseRedisServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // -------- Key-Value (String) --------

    @Override
    public void set(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    @Override
    public String getString(String key) {
        Object val = redisTemplate.opsForValue().get(key);
        return val == null ? null : String.valueOf(val);
    }

    // -------- TTL / Key ops --------

    @Override
    public boolean expireDays(String key, long days) {
        Boolean ok = redisTemplate.expire(key, Duration.ofDays(days));
        return Boolean.TRUE.equals(ok);
    }

    @Override
    public boolean expire(String key, Duration ttl) {
        Boolean ok = redisTemplate.expire(key, ttl);
        return Boolean.TRUE.equals(ok);
    }

    @Override
    public Long ttlSeconds(String key) {
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        return ttl; // có thể là -1 (không TTL) hoặc -2 (không tồn tại)
    }

    @Override
    public Boolean del(String key) {
        return redisTemplate.delete(key);
    }

    @Override
    public Boolean exists(String key) {
        Boolean ok = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(ok);
    }


    @Override
    public Set<String> hKeys(String key) {
        Set<Object> raw = redisTemplate.opsForHash().keys(key);
        if (raw == null || raw.isEmpty()) return Collections.emptySet();
        return raw.stream().map(String::valueOf)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public Long hSize(String key) {
        Long size = redisTemplate.opsForHash().size(key);
        return size == null ? 0L : size;
    }

    @Override
    public void hDel(String key, String field) {
        redisTemplate.opsForHash().delete(key, field);
    }

    @Override
    public void hDel(String key, List<String> fields) {
        if (fields == null || fields.isEmpty()) return;
        redisTemplate.opsForHash().delete(key, fields.toArray());
    }

    @Override
    public Long hIncrBy(String key, String field, long delta) {
        // HINCRBY: tăng/giảm atomic, auto tạo field nếu chưa có (mặc định 0)
        return redisTemplate.opsForHash().increment(key, field, delta);
    }

    @Override
    public Long cartIncr(String key, String field, long delta, Duration refreshTtlIfAny) {
        Long newQty = redisTemplate.opsForHash().increment(key, field, delta);
        if (newQty != null && newQty <= 0) {
            // Nếu số lượng về 0 hoặc âm -> xóa field khỏi giỏ
            redisTemplate.opsForHash().delete(key, field);
            newQty = 0L;
        }
        // Gia hạn TTL giỏ nếu có yêu cầu (vd Duration.ofDays(30))
        if (refreshTtlIfAny != null) {
            redisTemplate.expire(key, refreshTtlIfAny);
        }
        return newQty;
    }

}
