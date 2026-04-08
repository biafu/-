package com.zhinengpt.campuscatering.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DispatchRequest {

    @NotNull
    private Long orderId;

    @NotNull
    private Long deliveryUserId;

    private String dispatchRemark;
}
