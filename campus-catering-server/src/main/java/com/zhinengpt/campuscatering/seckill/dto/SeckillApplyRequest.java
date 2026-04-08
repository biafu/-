package com.zhinengpt.campuscatering.seckill.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SeckillApplyRequest {

    @NotNull
    private Long activityId;

    @NotBlank
    private String receiverName;

    @NotBlank
    private String receiverPhone;

    @NotBlank
    private String receiverAddress;

    private String remark;
}
