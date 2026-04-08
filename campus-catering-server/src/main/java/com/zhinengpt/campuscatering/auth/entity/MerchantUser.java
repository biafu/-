package com.zhinengpt.campuscatering.auth.entity;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class MerchantUser {

    private Long id;
    private Long merchantId;
    private String username;
    private String password;
    private String realName;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
