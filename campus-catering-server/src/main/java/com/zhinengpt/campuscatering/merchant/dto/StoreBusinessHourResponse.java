package com.zhinengpt.campuscatering.merchant.dto;

import java.time.LocalTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StoreBusinessHourResponse {

    private Long id;
    private Integer dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer status;
}
