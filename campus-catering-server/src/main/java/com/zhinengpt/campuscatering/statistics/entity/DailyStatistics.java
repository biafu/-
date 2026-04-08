package com.zhinengpt.campuscatering.statistics.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class DailyStatistics {

    private Long id;
    private LocalDate statDate;
    private Long orderCount;
    private Long completedOrderCount;
    private Long cancelledOrderCount;
    private BigDecimal gmv;
    private Long activeUserCount;
    private Long activeMerchantCount;
    private BigDecimal cancelRate;
    private Integer avgFulfillmentMinutes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
