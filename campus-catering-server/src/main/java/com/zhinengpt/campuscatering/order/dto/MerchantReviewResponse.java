package com.zhinengpt.campuscatering.order.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MerchantReviewResponse {

    private Long orderId;
    private Long orderNo;
    private Long storeId;
    private String storeName;
    private Integer rating;
    private String content;
    private Integer isAnonymous;
    private String reviewerName;
    private LocalDateTime createdAt;
}
