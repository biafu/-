package com.zhinengpt.campuscatering.trace.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderLogResponse {

    private String orderStatus;
    private String actionType;
    private String operatorType;
    private Long operatorId;
    private String content;
    private LocalDateTime createdAt;
}
