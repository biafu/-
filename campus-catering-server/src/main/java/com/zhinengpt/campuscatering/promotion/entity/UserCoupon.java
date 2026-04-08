package com.zhinengpt.campuscatering.promotion.entity;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class UserCoupon {

    private Long id;
    private Long userId;
    private Long couponId;
    private Integer status;
    private Long usedOrderId;
    private LocalDateTime usedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
