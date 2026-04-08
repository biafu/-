package com.zhinengpt.campuscatering.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartAddRequest {

    @NotNull
    private Long storeId;

    @NotNull
    private Long skuId;

    @NotNull
    @Min(1)
    private Integer quantity;
}
