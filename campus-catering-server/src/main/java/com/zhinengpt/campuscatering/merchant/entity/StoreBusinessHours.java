package com.zhinengpt.campuscatering.merchant.entity;

import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.Data;

@Data
public class StoreBusinessHours {

    private Long id;
    private Long storeId;
    private Integer dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
