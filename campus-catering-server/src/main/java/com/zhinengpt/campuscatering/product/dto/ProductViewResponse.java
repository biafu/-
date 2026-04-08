package com.zhinengpt.campuscatering.product.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductViewResponse {

    private Long spuId;
    private Long categoryId;
    private String productName;
    private Integer productType;
    private String imageUrl;
    private String description;
    private String comboDesc;
    private Integer saleStatus;
    private Integer sortNo;
    private List<SkuView> skus;
    private List<ComboItemView> comboItems;
}
