package com.zhinengpt.campuscatering.statistics.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MerchantRankResponse {

    private Long merchantId;
    private String merchantName;
    private Long orderCount;
    private BigDecimal gmv;
}
