package com.zhinengpt.campuscatering.student.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserAddressSaveRequest {

    private Long id;

    @NotBlank
    private String campusName;

    @NotBlank
    private String buildingName;

    @NotBlank
    private String roomNo;

    private String detailAddress;

    @NotBlank
    private String contactName;

    @NotBlank
    private String contactPhone;

    @NotNull
    private Integer isDefault;
}
