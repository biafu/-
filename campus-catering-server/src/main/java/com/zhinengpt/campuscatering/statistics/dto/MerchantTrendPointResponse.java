package com.zhinengpt.campuscatering.statistics.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MerchantTrendPointResponse {

    private LocalDate statDate;
    private Long orderCount;
    private BigDecimal revenue;
    private BigDecimal cancelRate;
}
