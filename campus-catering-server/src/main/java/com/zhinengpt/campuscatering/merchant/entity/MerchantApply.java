package com.zhinengpt.campuscatering.merchant.entity;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class MerchantApply {

    private Long id;
    private String merchantName;
    private String contactName;
    private String contactPhone;
    private String campusCode;
    private String licenseNo;
    private Integer status;
    private String auditRemark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
