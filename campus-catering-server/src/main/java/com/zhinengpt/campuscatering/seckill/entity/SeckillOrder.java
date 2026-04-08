package com.zhinengpt.campuscatering.seckill.entity;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class SeckillOrder {

    private Long id;
    private Long activityId;
    private Long userId;
    private Long orderId;
    private Long orderNo;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
