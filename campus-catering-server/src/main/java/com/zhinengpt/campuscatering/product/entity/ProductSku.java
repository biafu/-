package com.zhinengpt.campuscatering.product.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ProductSku {

    private Long id;
    private Long spuId;
    private String skuName;
    private BigDecimal price;
    private Integer stock;
    private Integer soldNum;
    private Integer version;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
