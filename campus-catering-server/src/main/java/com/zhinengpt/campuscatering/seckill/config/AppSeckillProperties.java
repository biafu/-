package com.zhinengpt.campuscatering.seckill.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.seckill")
public class AppSeckillProperties {

    private int queueCapacity = 20000;
    private int resultTtlMinutes = 30;
    private boolean failFastOnInit = false;
}
