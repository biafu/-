package com.zhinengpt.campuscatering.product.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class ProductEditorUpdateRequest {

    @NotNull
    private Long categoryId;

    @NotBlank
    private String productName;

    private String imageUrl;

    private String description;

    private Integer sortNo = 0;

    @NotNull
    private Long skuId;

    @NotBlank
    private String skuName;

    @NotNull
    private BigDecimal price;

    @NotNull
    private Integer stock;

    @NotNull
    private Integer skuStatus;

    private String comboDesc;

    @Valid
    private List<ComboItemSaveRequest> comboItems;
}
