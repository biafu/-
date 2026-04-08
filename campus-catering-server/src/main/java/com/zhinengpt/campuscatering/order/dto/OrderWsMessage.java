package com.zhinengpt.campuscatering.order.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderWsMessage {

    private String event;
    private Long merchantId;
    private Long orderId;
    private Long orderNo;
    private String orderStatus;
    private String message;
}
