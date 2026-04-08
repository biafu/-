package com.zhinengpt.campuscatering.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.order")
public class AppOrderProperties {

    private int unpaidTimeoutMinutes = 15;
    private int merchantAcceptTimeoutMinutes = 20;
    private int deliveryTimeoutMinutes = 60;
    private int alertDedupMinutes = 120;
    private int urgeCooldownSeconds = 120;
}
