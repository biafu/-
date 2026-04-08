package com.zhinengpt.campuscatering.order.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderCreateResponse {

    private Long orderId;
    private Long orderNo;
    private String orderStatus;
    private BigDecimal payAmount;
}
