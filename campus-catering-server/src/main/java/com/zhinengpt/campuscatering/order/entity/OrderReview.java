package com.zhinengpt.campuscatering.order.entity;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class OrderReview {

    private Long id;
    private Long orderId;
    private Long userId;
    private Long merchantId;
    private Long storeId;
    private Integer rating;
    private String content;
    private Integer isAnonymous;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
