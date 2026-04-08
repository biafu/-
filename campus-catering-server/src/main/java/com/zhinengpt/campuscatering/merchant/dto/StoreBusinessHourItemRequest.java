package com.zhinengpt.campuscatering.merchant.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import lombok.Data;

@Data
public class StoreBusinessHourItemRequest {

    @NotNull
    @Min(1)
    @Max(7)
    private Integer dayOfWeek;

    @NotNull
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime startTime;

    @NotNull
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime endTime;

    @NotNull
    @Min(0)
    @Max(1)
    private Integer status;
}
