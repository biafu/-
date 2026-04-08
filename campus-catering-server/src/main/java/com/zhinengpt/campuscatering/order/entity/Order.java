package com.zhinengpt.campuscatering.order.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class Order {

    private Long id;
    private Long orderNo;
    private Long userId;
    private Long storeId;
    private Long merchantId;
    private Integer orderType;
    private String orderStatus;
    private BigDecimal goodsAmount;
    private BigDecimal deliveryFee;
    private BigDecimal discountAmount;
    private BigDecimal payAmount;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String remark;
    private LocalDateTime payTime;
    private LocalDateTime acceptTime;
    private LocalDateTime completeTime;
    private LocalDateTime cancelTime;
    private String cancelReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
