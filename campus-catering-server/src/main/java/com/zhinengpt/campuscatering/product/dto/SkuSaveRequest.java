package com.zhinengpt.campuscatering.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class SkuSaveRequest {

    @NotBlank
    private String skuName;

    @NotNull
    private BigDecimal price;

    @NotNull
    private Integer stock;
}
