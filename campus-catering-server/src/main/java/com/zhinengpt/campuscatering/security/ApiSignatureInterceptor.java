package com.zhinengpt.campuscatering.security;

import com.zhinengpt.campuscatering.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ApiSignatureInterceptor implements HandlerInterceptor {

    private final AppSecurityProperties properties;
    private final StringRedisTemplate stringRedisTemplate;

    public ApiSignatureInterceptor(AppSecurityProperties properties, StringRedisTemplate stringRedisTemplate) {
        this.properties = properties;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!properties.getApiSign().isEnabled()) {
            return true;
        }

        String requestUri = request.getRequestURI();
        if (requestUri.startsWith("/api/auth/")) {
            return true;
        }

        String apiKey = request.getHeader("X-API-KEY");
        String timestamp = request.getHeader("X-TIMESTAMP");
        String nonce = request.getHeader("X-NONCE");
        String sign = request.getHeader("X-SIGN");
        if (!StringUtils.hasText(apiKey) || !StringUtils.hasText(timestamp)
                || !StringUtils.hasText(nonce) || !StringUtils.hasText(sign)) {
            throw new BusinessException(4011, "请求签名缺失");
        }
        if (!properties.getApiSign().getApiKey().equals(apiKey)) {
            throw new BusinessException(4012, "无效的API KEY");
        }

        long requestSeconds = Long.parseLong(timestamp);
        long now = Instant.now().getEpochSecond();
        if (Math.abs(now - requestSeconds) > properties.getApiSign().getTimestampWindowSeconds()) {
            throw new BusinessException(4013, "请求已过期");
        }

        String raw = apiKey + "." + timestamp + "." + nonce + "." + request.getMethod() + "." + requestUri;
        String expectedSign = hmacSha256(raw, properties.getApiSign().getApiSecret());
        if (!expectedSign.equalsIgnoreCase(sign)) {
            throw new BusinessException(4014, "签名校验失败");
        }
        Boolean nonceAccepted = stringRedisTemplate.opsForValue().setIfAbsent(
                "api:nonce:" + apiKey + ":" + nonce,
                timestamp,
                java.time.Duration.ofSeconds(properties.getApiSign().getTimestampWindowSeconds())
        );
        if (Boolean.FALSE.equals(nonceAccepted)) {
            throw new BusinessException(4015, "重复请求，请勿重放");
        }
        return true;
    }

    private String hmacSha256(String content, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(content.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new BusinessException(5001, "签名服务不可用");
        }
    }
}
