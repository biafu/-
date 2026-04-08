package com.zhinengpt.campuscatering.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class TokenBlacklistService {

    private static final String TOKEN_BLACKLIST_PREFIX = "auth:jwt:blacklist:";

    private final StringRedisTemplate stringRedisTemplate;

    public TokenBlacklistService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void blacklist(String token, Instant expireAt) {
        Duration ttl = Duration.between(Instant.now(), expireAt);
        if (ttl.isNegative() || ttl.isZero()) {
            return;
        }
        stringRedisTemplate.opsForValue().set(buildKey(token), "1", ttl);
    }

    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(buildKey(token)));
    }

    private String buildKey(String token) {
        return TOKEN_BLACKLIST_PREFIX + sha256(token);
    }

    private String sha256(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm not supported", ex);
        }
    }
}
