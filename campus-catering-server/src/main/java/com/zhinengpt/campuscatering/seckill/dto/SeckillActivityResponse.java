package com.zhinengpt.campuscatering.seckill.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SeckillActivityResponse {

    private Long id;
    private Long storeId;
    private Long skuId;
    private String activityName;
    private BigDecimal seckillPrice;
    private Integer stock;
    private Integer totalStock;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
