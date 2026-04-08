package com.zhinengpt.campuscatering.order.entity;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class DeliveryOrder {

    private Long id;
    private Long orderId;
    private Long deliveryUserId;
    private Integer dispatchStatus;
    private LocalDateTime pickupTime;
    private LocalDateTime deliveredTime;
    private String dispatchRemark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
