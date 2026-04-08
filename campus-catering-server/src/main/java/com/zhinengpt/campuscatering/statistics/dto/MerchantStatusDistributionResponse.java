package com.zhinengpt.campuscatering.statistics.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MerchantStatusDistributionResponse {

    private String orderStatus;
    private Long orderCount;
}
