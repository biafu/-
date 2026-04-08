package com.zhinengpt.campuscatering.trace.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class PaymentRecord {

    private Long id;
    private Long orderId;
    private Long orderNo;
    private BigDecimal payAmount;
    private String payChannel;
    private String payStatus;
    private String transactionNo;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
