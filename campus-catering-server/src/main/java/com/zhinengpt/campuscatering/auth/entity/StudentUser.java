package com.zhinengpt.campuscatering.auth.entity;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class StudentUser {

    private Long id;
    private String phone;
    private String nickname;
    private String password;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
