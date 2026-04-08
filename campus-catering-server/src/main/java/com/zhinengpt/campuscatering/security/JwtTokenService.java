package com.zhinengpt.campuscatering.security;

import com.zhinengpt.campuscatering.common.enums.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

    private final AppSecurityProperties properties;
    private SecretKey secretKey;

    public JwtTokenService(AppSecurityProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void init() {
        byte[] keyBytes = properties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8);
        byte[] padded = keyBytes.length >= 32 ? keyBytes : (properties.getJwt().getSecret() + "-campus-catering-dev-secret").getBytes(StandardCharsets.UTF_8);
        this.secretKey = Keys.hmacShaKeyFor(padded);
    }

    public String generateToken(LoginUser loginUser) {
        Instant now = Instant.now();
        Instant expireAt = now.plus(properties.getJwt().getExpirationHours(), ChronoUnit.HOURS);
        return Jwts.builder()
                .subject(String.valueOf(loginUser.getUserId()))
                .claim("merchantId", loginUser.getMerchantId())
                .claim("username", loginUser.getUsername())
                .claim("role", loginUser.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expireAt))
                .signWith(secretKey)
                .compact();
    }

    public LoginUser parseToken(String token) {
        Claims claims = parseClaims(token);
        Long merchantId = claims.get("merchantId") == null ? null : Long.valueOf(String.valueOf(claims.get("merchantId")));
        return LoginUser.builder()
                .userId(Long.valueOf(claims.getSubject()))
                .merchantId(merchantId)
                .username(String.valueOf(claims.get("username")))
                .role(UserRole.valueOf(String.valueOf(claims.get("role"))))
                .build();
    }

    public Instant parseExpiration(String token) {
        return parseClaims(token).getExpiration().toInstant();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
