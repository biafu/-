package com.zhinengpt.campuscatering.product.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategorySaveRequest {

    @NotBlank
    private String categoryName;

    private Integer sortNo = 0;
}
