package com.zhinengpt.campuscatering.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderDetailResponse {

    private Long id;
    private Long orderNo;
    private String orderStatus;
    private Long storeId;
    private String storeName;
    private BigDecimal payAmount;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String remark;
    private LocalDateTime createdAt;
    private List<OrderItemVO> items;
}
