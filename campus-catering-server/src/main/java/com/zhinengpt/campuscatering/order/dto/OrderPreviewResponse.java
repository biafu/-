package com.zhinengpt.campuscatering.order.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderPreviewResponse {

    private BigDecimal goodsAmount;
    private BigDecimal deliveryFee;
    private BigDecimal discountAmount;
    private BigDecimal payAmount;
    private List<OrderPreviewSku> items;
}
