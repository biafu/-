package com.zhinengpt.campuscatering.student.entity;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class UserAddress {

    private Long id;
    private Long userId;
    private String campusName;
    private String buildingName;
    private String roomNo;
    private String detailAddress;
    private String contactName;
    private String contactPhone;
    private Integer isDefault;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
