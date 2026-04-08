package com.zhinengpt.campuscatering.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderExceptionResponse {

    private Long id;
    private Long orderId;
    private Long orderNo;
    private String orderStatus;
    private BigDecimal payAmount;
    private String reason;
    private String status;
    private String resolvedRemark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
