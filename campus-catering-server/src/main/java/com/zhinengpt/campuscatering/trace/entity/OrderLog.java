package com.zhinengpt.campuscatering.trace.entity;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class OrderLog {

    private Long id;
    private Long orderId;
    private String orderStatus;
    private String actionType;
    private String operatorType;
    private Long operatorId;
    private String content;
    private LocalDateTime createdAt;
}
