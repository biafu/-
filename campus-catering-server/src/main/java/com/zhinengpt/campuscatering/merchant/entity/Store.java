package com.zhinengpt.campuscatering.merchant.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class Store {

    private Long id;
    private Long merchantId;
    private String storeName;
    private String address;
    private Integer deliveryType;
    private String deliveryScopeDesc;
    private BigDecimal deliveryRadiusKm;
    private BigDecimal minOrderAmount;
    private BigDecimal deliveryFee;
    private Integer businessStatus;
    private String notice;
    private Integer deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
