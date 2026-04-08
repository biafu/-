package com.zhinengpt.campuscatering.merchant.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;

@Data
public class StoreBusinessHoursSaveRequest {

    @Valid
    @NotEmpty
    private List<StoreBusinessHourItemRequest> hours;
}
