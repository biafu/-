package com.zhinengpt.campuscatering.order.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderReviewResponse {

    private Long orderId;
    private Integer rating;
    private String content;
    private Integer isAnonymous;
    private LocalDateTime createdAt;
}
