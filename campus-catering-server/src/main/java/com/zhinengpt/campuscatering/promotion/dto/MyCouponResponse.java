package com.zhinengpt.campuscatering.promotion.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class MyCouponResponse {

    private Long userCouponId;
    private Long couponId;
    private String couponName;
    private BigDecimal discountAmount;
    private BigDecimal thresholdAmount;
    private Long storeId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
}
