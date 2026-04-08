package com.zhinengpt.campuscatering.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderExceptionHandleRequest {

    @NotNull
    private Long orderId;

    @NotBlank
    private String reason;
}
