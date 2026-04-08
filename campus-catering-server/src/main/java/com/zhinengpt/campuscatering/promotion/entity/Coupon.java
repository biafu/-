package com.zhinengpt.campuscatering.promotion.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class Coupon {

    private Long id;
    private String couponName;
    private Integer couponType;
    private BigDecimal discountAmount;
    private BigDecimal thresholdAmount;
    private Long storeId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer totalCount;
    private Integer receiveCount;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
