package com.zhinengpt.campuscatering.merchant.entity;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class Merchant {

    private Long id;
    private String merchantName;
    private String contactName;
    private String contactPhone;
    private Integer status;
    private Integer settleType;
    private String campusCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
