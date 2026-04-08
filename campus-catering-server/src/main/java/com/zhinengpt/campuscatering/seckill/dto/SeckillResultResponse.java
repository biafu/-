package com.zhinengpt.campuscatering.seckill.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SeckillResultResponse {

    private String requestId;
    private String status;
    private String message;
    private Long orderId;
    private Long orderNo;
}
