package com.zhinengpt.campuscatering.product.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class ProductSaveRequest {

    @NotNull
    private Long categoryId;

    @NotBlank
    private String productName;

    @NotNull
    private Integer productType;

    private String imageUrl;

    private String description;

    private Integer sortNo = 0;

    private String comboDesc;

    @Valid
    private List<ComboItemSaveRequest> comboItems;

    @Valid
    @NotEmpty
    private List<SkuSaveRequest> skus;
}
