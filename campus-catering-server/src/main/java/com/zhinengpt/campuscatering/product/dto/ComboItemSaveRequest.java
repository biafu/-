package com.zhinengpt.campuscatering.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ComboItemSaveRequest {

    @NotNull
    private Long skuId;

    @NotNull
    @Min(1)
    private Integer quantity;

    private Integer sortNo = 0;
}
