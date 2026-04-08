package com.zhinengpt.campuscatering.statistics.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MerchantOverviewResponse {

    private Long todayOrderCount;
    private Long totalOrderCount;
    private BigDecimal todayRevenue;
}
