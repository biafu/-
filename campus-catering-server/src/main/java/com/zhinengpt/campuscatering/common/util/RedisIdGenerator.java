package com.zhinengpt.campuscatering.common.util;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisIdGenerator {

    private static final long BEGIN_TIMESTAMP = 1704067200L;
    private static final AtomicLong LOCAL_FALLBACK = new AtomicLong(1L);

    private final StringRedisTemplate stringRedisTemplate;

    public RedisIdGenerator(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public long nextId(String keyPrefix) {
        long nowSecond = java.time.LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8"));
        long timestampOffset = nowSecond - BEGIN_TIMESTAMP;
        long sequence;
        try {
            String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
            sequence = stringRedisTemplate.opsForValue().increment("order:id:inc:" + keyPrefix + ":" + date);
        } catch (Exception ex) {
            sequence = LOCAL_FALLBACK.getAndIncrement();
        }
        return (timestampOffset << 32) | sequence;
    }
}
