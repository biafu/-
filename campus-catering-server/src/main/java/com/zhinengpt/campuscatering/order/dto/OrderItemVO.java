package com.zhinengpt.campuscatering.order.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderItemVO {

    private Long skuId;
    private String spuName;
    private String skuName;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal totalAmount;
}
