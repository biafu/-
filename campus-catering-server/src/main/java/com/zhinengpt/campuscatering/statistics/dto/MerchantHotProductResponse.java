package com.zhinengpt.campuscatering.statistics.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MerchantHotProductResponse {

    private String spuName;
    private String skuName;
    private Long soldQuantity;
    private BigDecimal gmv;
}
