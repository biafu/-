package com.zhinengpt.campuscatering.statistics.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardOverviewResponse {

    private Long orderCount;
    private BigDecimal totalRevenue;
    private Long merchantCount;
    private Long activeUserCount;
}
