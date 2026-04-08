package com.zhinengpt.campuscatering.cart.entity;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ShoppingCart {

    private Long id;
    private Long userId;
    private Long storeId;
    private Long skuId;
    private Integer quantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
