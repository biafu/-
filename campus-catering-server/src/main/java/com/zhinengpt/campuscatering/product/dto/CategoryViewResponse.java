package com.zhinengpt.campuscatering.product.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryViewResponse {

    private Long id;
    private String categoryName;
    private Integer sortNo;
}
