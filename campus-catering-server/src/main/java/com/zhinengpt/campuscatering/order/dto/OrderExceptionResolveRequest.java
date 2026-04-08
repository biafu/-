package com.zhinengpt.campuscatering.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderExceptionResolveRequest {

    @NotNull
    private Long orderId;

    @NotBlank
    private String resolvedRemark;
}
