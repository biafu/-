package com.zhinengpt.campuscatering;

import com.zhinengpt.campuscatering.order.config.AppOrderProperties;
import com.zhinengpt.campuscatering.security.AppSecurityProperties;
import com.zhinengpt.campuscatering.seckill.config.AppSeckillProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties({AppSecurityProperties.class, AppOrderProperties.class, AppSeckillProperties.class})
public class CampusCateringApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusCateringApplication.class, args);
    }
}
