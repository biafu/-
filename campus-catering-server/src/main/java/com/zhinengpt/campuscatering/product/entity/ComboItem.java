package com.zhinengpt.campuscatering.product.entity;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ComboItem {

    private Long id;
    private Long comboId;
    private Long skuId;
    private Integer quantity;
    private Integer sortNo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
