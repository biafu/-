package com.zhinengpt.campuscatering.product.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SkuView {

    private Long skuId;
    private String skuName;
    private BigDecimal price;
    private Integer stock;
    private Integer status;
}
