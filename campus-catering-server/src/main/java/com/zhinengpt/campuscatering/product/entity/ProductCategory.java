package com.zhinengpt.campuscatering.product.entity;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ProductCategory {

    private Long id;
    private Long storeId;
    private String categoryName;
    private Integer sortNo;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
