package com.zhinengpt.campuscatering.product.entity;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ProductSpu {

    private Long id;
    private Long storeId;
    private Long categoryId;
    private String productName;
    private Integer productType;
    private String imageUrl;
    private String description;
    private Integer saleStatus;
    private Integer sortNo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
