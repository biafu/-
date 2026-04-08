package com.zhinengpt.campuscatering.product.entity;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class Combo {

    private Long id;
    private Long spuId;
    private String comboDesc;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
