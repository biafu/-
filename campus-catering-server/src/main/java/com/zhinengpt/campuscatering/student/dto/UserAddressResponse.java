package com.zhinengpt.campuscatering.student.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserAddressResponse {

    private Long id;
    private String campusName;
    private String buildingName;
    private String roomNo;
    private String detailAddress;
    private String contactName;
    private String contactPhone;
    private Integer isDefault;
    private String fullAddress;
    private LocalDateTime updatedAt;
}
