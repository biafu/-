package com.zhinengpt.campuscatering.merchant.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StoreSimpleResponse {

    private Long id;
    private Long merchantId;
    private String storeName;
    private String address;
    private String deliveryScopeDesc;
    private BigDecimal deliveryRadiusKm;
    private BigDecimal minOrderAmount;
    private BigDecimal deliveryFee;
    private Integer businessStatus;
    private String notice;
}
