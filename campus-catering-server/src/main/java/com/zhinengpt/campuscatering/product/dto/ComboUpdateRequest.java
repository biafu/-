package com.zhinengpt.campuscatering.product.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;

@Data
public class ComboUpdateRequest {

    private String comboDesc;

    @Valid
    @NotEmpty
    private List<ComboItemSaveRequest> comboItems;
}
