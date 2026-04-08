package com.zhinengpt.campuscatering.merchant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class StoreUpdateRequest {

    @NotBlank
    private String storeName;

    @NotBlank
    private String address;

    @NotNull
    private Integer deliveryType;

    private String deliveryScopeDesc;

    @NotNull
    private BigDecimal deliveryRadiusKm;

    @NotNull
    private BigDecimal minOrderAmount;

    @NotNull
    private BigDecimal deliveryFee;

    @NotNull
    private Integer businessStatus;

    private String notice;
}
