package com.badmintonhub.cartservice.service.base.impl;

import com.badmintonhub.cartservice.service.base.BaseRedisService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class BaseRedisServiceImpl implements BaseRedisService {
    private final RedisTemplate<String, Object> redisTemplate;

    public BaseRedisServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean expire(String key, Duration ttl) {
        Boolean ok = redisTemplate.expire(key, ttl);
        return Boolean.TRUE.equals(ok);
    }

    @Override
    public Boolean del(String key) {
        return redisTemplate.delete(key);
    }

    @Override
    public Object hGet(String key, String field) {
        return redisTemplate.opsForHash().get(key, field);
    }

    @Override
    public void hDel(String key, String field) {
        redisTemplate.opsForHash().delete(key, field);
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
