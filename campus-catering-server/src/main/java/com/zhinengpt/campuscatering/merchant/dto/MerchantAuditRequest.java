package com.zhinengpt.campuscatering.merchant.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MerchantAuditRequest {

    @NotNull
    private Long applyId;

    @NotNull
    private Boolean approved;

    private String auditRemark;
}
