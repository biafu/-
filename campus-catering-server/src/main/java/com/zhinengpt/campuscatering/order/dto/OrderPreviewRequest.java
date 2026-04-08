package com.zhinengpt.campuscatering.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class OrderPreviewRequest {

    @NotNull
    private Long storeId;

    @NotBlank
    private String receiverName;

    @NotBlank
    private String receiverPhone;

    @NotBlank
    private String receiverAddress;

    private Long userCouponId;

    private String remark;

    @Valid
    @NotEmpty
    private List<OrderItemRequest> items;
}
