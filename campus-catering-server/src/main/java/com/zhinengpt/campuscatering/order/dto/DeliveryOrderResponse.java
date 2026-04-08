package com.zhinengpt.campuscatering.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeliveryOrderResponse {

    private Long orderId;
    private Long orderNo;
    private Long storeId;
    private String storeName;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String orderStatus;
    private Integer dispatchStatus;
    private String dispatchRemark;
    private BigDecimal payAmount;
    private LocalDateTime createdAt;
    private LocalDateTime pickupTime;
    private LocalDateTime deliveredTime;
}
