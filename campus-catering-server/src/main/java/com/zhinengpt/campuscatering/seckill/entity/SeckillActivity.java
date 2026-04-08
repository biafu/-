package com.zhinengpt.campuscatering.seckill.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class SeckillActivity {

    private Long id;
    private Long storeId;
    private Long skuId;
    private String activityName;
    private BigDecimal seckillPrice;
    private Integer stock;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
