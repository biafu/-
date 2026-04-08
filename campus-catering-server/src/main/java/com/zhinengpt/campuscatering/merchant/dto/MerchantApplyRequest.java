package com.zhinengpt.campuscatering.merchant.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MerchantApplyRequest {

    @NotBlank
    private String merchantName;

    @NotBlank
    private String contactName;

    @NotBlank
    private String contactPhone;

    @NotBlank
    private String campusCode;

    @NotBlank
    private String licenseNo;
}
