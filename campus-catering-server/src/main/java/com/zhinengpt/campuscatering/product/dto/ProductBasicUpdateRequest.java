package com.zhinengpt.campuscatering.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductBasicUpdateRequest {

    @NotNull
    private Long categoryId;

    @NotBlank
    private String productName;

    private String imageUrl;

    private String description;

    private Integer sortNo = 0;
}
