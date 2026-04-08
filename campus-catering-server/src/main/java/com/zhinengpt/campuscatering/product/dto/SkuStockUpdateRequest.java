package com.zhinengpt.campuscatering.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SkuStockUpdateRequest {

    @NotNull
    @Min(0)
    private Integer stock;
}
