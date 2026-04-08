package com.zhinengpt.campuscatering.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.security")
public class AppSecurityProperties {

    private Jwt jwt = new Jwt();
    private ApiSign apiSign = new ApiSign();

    @Data
    public static class Jwt {
        private String secret;
        private long expirationHours = 24;
    }

    @Data
    public static class ApiSign {
        private boolean enabled = false;
        private String apiKey;
        private String apiSecret;
        private long timestampWindowSeconds = 300;
    }
}
