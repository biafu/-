package com.zhinengpt.campuscatering.cart.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartItemResponse {

    private Long id;
    private Long storeId;
    private Long skuId;
    private String productName;
    private String imageUrl;
    private String skuName;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal totalAmount;
}
