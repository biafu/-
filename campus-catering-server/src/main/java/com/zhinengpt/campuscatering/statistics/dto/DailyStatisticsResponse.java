package com.zhinengpt.campuscatering.statistics.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DailyStatisticsResponse {

    private LocalDate statDate;
    private Long orderCount;
    private Long completedOrderCount;
    private Long cancelledOrderCount;
    private BigDecimal gmv;
    private Long activeUserCount;
    private Long activeMerchantCount;
    private BigDecimal cancelRate;
    private Integer avgFulfillmentMinutes;
}
