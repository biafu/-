package com.zhinengpt.campuscatering.seckill.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SeckillApplyResponse {

    private String requestId;
    private String status;
    private String message;
}
