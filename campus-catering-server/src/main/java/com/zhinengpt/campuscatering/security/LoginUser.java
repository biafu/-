package com.zhinengpt.campuscatering.security;

import com.zhinengpt.campuscatering.common.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser {

    private Long userId;
    private Long merchantId;
    private String username;
    private UserRole role;
}
