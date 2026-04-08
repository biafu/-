package com.zhinengpt.campuscatering.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhinengpt.campuscatering.common.exception.BusinessException;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class CacheClient {

    private static final String NULL_VALUE = "__null__";

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public CacheClient(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    public void set(String key, Object value, Duration ttl) {
        try {
            stringRedisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), ttl);
        } catch (JsonProcessingException ex) {
            throw new BusinessException(5002, "缓存序列化失败");
        }
    }

    public void setWithRandomTtl(String key, Object value, Duration baseTtl, int randomSeconds) {
        Duration ttl = baseTtl.plusSeconds(ThreadLocalRandom.current().nextInt(Math.max(randomSeconds, 1)));
        set(key, value, ttl);
    }

    public void delete(String key) {
        stringRedisTemplate.delete(key);
    }

    public <T> T queryWithPassThrough(String key, Class<T> type, Duration ttl, Duration nullTtl, Function<String, T> dbFallback) {
        String json = stringRedisTemplate.opsForValue().get(key);
        if (NULL_VALUE.equals(json)) {
            return null;
        }
        if (StringUtils.hasText(json)) {
            try {
                return objectMapper.readValue(json, type);
            } catch (JsonProcessingException ex) {
                throw new BusinessException(5003, "缓存反序列化失败");
            }
        }
        T result = dbFallback.apply(key);
        if (result == null) {
            stringRedisTemplate.opsForValue().set(key, NULL_VALUE, nullTtl);
            return null;
        }
        set(key, result, ttl);
        return result;
    }

    public <T> T queryWithMutex(String key, String lockKey, Class<T> type, Duration ttl, Duration nullTtl, Function<String, T> dbFallback) {
        String json = stringRedisTemplate.opsForValue().get(key);
        if (NULL_VALUE.equals(json)) {
            return null;
        }
        if (StringUtils.hasText(json)) {
            try {
                return objectMapper.readValue(json, type);
            } catch (JsonProcessingException ex) {
                throw new BusinessException(5004, "缓存反序列化失败");
            }
        }

        Boolean locked = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, "1", Duration.ofSeconds(10));
        if (Boolean.TRUE.equals(locked)) {
            try {
                T result = dbFallback.apply(key);
                if (result == null) {
                    stringRedisTemplate.opsForValue().set(key, NULL_VALUE, nullTtl);
                    return null;
                }
                setWithRandomTtl(key, result, ttl, 300);
                return result;
            } finally {
                stringRedisTemplate.delete(lockKey);
            }
        }

        try {
            Thread.sleep(50L);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        return queryWithPassThrough(key, type, ttl, nullTtl, dbFallback);
    }
}
