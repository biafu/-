package com.zhinengpt.campuscatering.order.entity;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class OrderException {

    private Long id;
    private Long orderId;
    private Long merchantId;
    private String status;
    private String reason;
    private String resolvedRemark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
