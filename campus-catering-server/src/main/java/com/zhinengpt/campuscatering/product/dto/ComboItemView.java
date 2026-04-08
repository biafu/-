package com.zhinengpt.campuscatering.product.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ComboItemView {

    private Long skuId;
    private String spuName;
    private String skuName;
    private Integer quantity;
    private Integer sortNo;
}
