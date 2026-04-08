package com.zhinengpt.campuscatering.order.entity;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class OrderItem {

    private Long id;
    private Long orderId;
    private Long skuId;
    private String spuName;
    private String skuName;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal totalAmount;
}
