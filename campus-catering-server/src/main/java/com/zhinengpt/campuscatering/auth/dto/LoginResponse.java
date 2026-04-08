package com.zhinengpt.campuscatering.auth.dto;

import com.zhinengpt.campuscatering.common.enums.UserRole;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {

    private Long userId;
    private Long merchantId;
    private String username;
    private UserRole role;
    private String token;
}
